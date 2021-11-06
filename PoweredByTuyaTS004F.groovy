/**
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 * 
 *  Test DH based on "Zemismart Button", namespace: "SangBoy", author: "YooSangBeom"
 * 
 * rev 1.0 2021-05-08 kkossev - inital test version
 * rev 1.1 2021-06-06 kkossev - added 'held' and 'up_hold' (release) events decoding for the 2 right buttons (3 and 4)
 * rev 1.2 2021-06-10 kkossev - changed the buttons numbers to match other similar Scene switches ( T S 0 0 4 4 for example):
 * ---------
 * ! 4 ! 3 !
 * ---------
 * ! 1 ! 2 !
 * ---------   Button 1 is the one that must be pressed ~10 seconds to start the zigbee pairing process
 *
 * rev 2.0 2021-10-31 kkossev - initialize TS004F in Scene mode during zigbee pairing. Process both Dimmer and Scene mode keypresses!; added Preferencies:	logEnable, txtEnable, reverseButton
 * rev 2.1 2021-11-06 kkossev - optimized configuration; removed reverseButton settings; debug logging is now true by default
 *  
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
  definition (name: "Powered by Tuya TS004F", namespace: "smartthings", author: "kkossev", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
	  capability "Refresh"
	  capability "Button"
      capability "Momentary"
	  capability "Health Check"
      
      command "configure"
      command refresh
      
 	  fingerprint inClusters: "0000,0001,0003,0004,0006,1000", outClusters: "0019,000A,0003,0004,0005,0006,0008,1000", manufacturer: "_TZ3000_xabckq1v", model: "TS004F", deviceJoinName: "Powered by Tuya 4 Button TS004F", mnmn: "SmartThings", vid: "generic-4-button" 
	}
    
    preferences {
        input (name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true)
        input (name: "txtEnable", type: "bool", title: "Enable description text logging", defaultValue: true)
    }
    
	tiles(scale: 2)
	{  
      multiAttributeTile(name: "button", type: "generic", width: 2, height: 2) 
      {
         tileAttribute("device.button", key: "PRIMARY_CONTROL") 
         {
            attributeState "pushed", label: "Pressed", icon:"st.Weather.weather14", backgroundColor:"#53a7c0"
            attributeState "double", label: "Pressed Twice", icon:"st.Weather.weather11", backgroundColor:"#53a7c0"
            attributeState "held", label: "Held", icon:"st.Weather.weather13", backgroundColor:"#53a7c0"
         }
      }
      valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) 
      {
         state "battery", label: '${currentValue}% battery', unit: ""
      }
      standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) 
      {
         state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
      }

      main(["button"])
      details(["button","battery", "refresh"])
	}    
   
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	def result = []
	def buttonNumber = 0
	if (event) {
		sendEvent(event)
        log.debug "sendEvent $event"
        return
	}
	if (description?.startsWith("catchall:")) {
    	def descMap = zigbee.parseDescriptionAsMap(description)            
    	log.debug "catchall descMap: $descMap"
    	def buttonState = "unknown"
    	// Scene mode command "FD"
    	if (descMap.clusterInt == 0x0006 && descMap.command == "FD") {
			if (descMap.sourceEndpoint == "03") {
     			buttonNumber = 1
        	}
        	else if (descMap.sourceEndpoint == "04") {
      	    	buttonNumber = 2
        	}
        	else if (descMap.sourceEndpoint == "02") {
            	buttonNumber = 3
        	}
        	else if (descMap.sourceEndpoint == "01") {
       	    	buttonNumber = 4
        	}    
			state.lastButtonNumber = buttonNumber
       		if (descMap.data[0] == "00")
           		buttonState = "pushed"
       		else if (descMap.data[0] == "01")
           		buttonState = "double"
       		else if (descMap.data[0] == "02")
           		buttonState = "held"
       		else {
           		if (logEnable) {log.warn "unkknown data in event from cluster ${descMap.clusterInt} sourceEndpoint ${descMap.sourceEndpoint} data[0] = ${descMap.data[0]}"}
 	       		return null 
        	}
    	}
    	// TS004F in Dimmer mode
    	else {
    		if (descMap.clusterInt == 0x0008 && descMap.command == "01" && descMap.data[0] == "00") {
      			buttonNumber = 3
      			buttonState = "held"
    		}
    		else if (descMap.clusterInt == 0x0008 && descMap.command == "01" && descMap.data[0] == "01") {
      			buttonNumber = 2
      			buttonState = "held"
    		}
    		else if (descMap.clusterInt == 0x0006 && descMap.command == "00" ) {
    			buttonNumber = 1
    			buttonState = "pushed"
    		}
    		else if (descMap.clusterInt == 0x0006 && descMap.command == "01") {
     			buttonNumber = 4
    			buttonState = "pushed"
    		}
    		else if (descMap.clusterInt == 0x0008 && descMap.data[0] == "00") {
     			buttonNumber = 3
    			buttonState = "pushed"
    		}
    		else if (descMap.clusterInt == 0x0008 && descMap.data[0] == "01") {
    			buttonNumber = 2
    			buttonState = "pushed"
    		}
    		else if (descMap.clusterInt == 0x0008 && descMap.command == "03") {
      			buttonNumber = state.lastButtonNumber
      			buttonState = "up_hold"			// was "up_hold"
			}
	    	else {
				log.warn "DID NOT PARSE MESSAGE for description : $description"
				log.debug zigbee.parseDescriptionAsMap(description)
       			return null
  			}
		}
    	//
   		state.lastButtonNumber = buttonNumber
		if (buttonState in ["pushed","double","held","up_hold"] &&  buttonNumber != 0) {
	   		def descriptionText = "button $buttonNumber was $buttonState"
	   		event = [name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true, displayed: true]
            sendButtonEvent(buttonNumber, buttonState)
   		}	
    } // if catchall
	if (event) {
	    log.info "Creating event: ${event}"
		result = createEvent(event)
	} 
  	return result
}


private sendButtonEvent(buttonNumber, buttonState) 
{
   def child = childDevices?.find { channelNumber(it.deviceNetworkId) == buttonNumber }
   if (child)
   {
      def descriptionText = "$child.displayName was $buttonState" // TODO: Verify if this is needed, and if capability template already has it handled
      log.debug "child $child"
      child?.sendEvent([name: "button", value: buttonState, data: [buttonNumber: 1], descriptionText: descriptionText, isStateChange: true])
   } 
   else 
   {
      log.debug "Child device $buttonNumber not found!"
   }
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	log.debug "refresh..."
	//zigbee.onOffRefresh() + zigbee.onOffConfig()
}

def configure() {
	log.debug "configure"
	List cmd = []

	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0x0000 0xfffe")
	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0x0006 0x8004")
	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0xE001 0xD011")
	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0x0001 0x0020")
	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0x0001 0x0021")
	cmd.add("st wattr 0x${device.deviceNetworkId} 1 0x0006 0x8004 0x30 {01}")
	cmd.add("st rattr 0x${device.deviceNetworkId} 1 0x0006 0x8004")
    
    cmd.add("zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}")
    cmd.add("zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}")
    cmd.add("zdo bind 0x${device.deviceNetworkId} 0x03 0x01 0x0006 {${device.zigbeeId}} {}")
    cmd.add("zdo bind 0x${device.deviceNetworkId} 0x04 0x01 0x0006 {${device.zigbeeId}} {}")

	sendHubCommand(cmd, 25)
}
    
private channelNumber(String dni) 
{
   dni.split(":")[-1] as Integer
}
    
    
private getButtonName(buttonNum) 
{
   return "${device.displayName} " + buttonNum
}
    
private void createChildButtonDevices_1(numberOfButtons) 
{
   state.oldLabel = device.label
   log.debug "Creating $numberOfButtons"
   log.debug "Creating $numberOfButtons children"
   
   for (i in 1..numberOfButtons) 
   {
      log.debug "Creating child $i"
      def child = addChildDevice("smartthings", "Child Button", "${device.deviceNetworkId}:${i}", device.hubId,[completedSetup: true, label: getButtonName(i),
				 isComponent: true, componentName: "button$i", componentLabel: "buttton ${i}"])
      child.sendEvent(name: "supportedButtonValues", value: ["pushed", "double", "held", "up_hold"].encodeAsJSON(), displayed: false)
      child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
      child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
   }
} 

def installed() 
{
    log.info "installed ..."
    
    def numberOfButtons = 4
    createChildButtonDevices_1(numberOfButtons)
    
    sendEvent(name: "supportedButtonValues", value: ["pushed", "double", "held", "up_hold"].encodeAsJSON(), displayed: false)
    sendEvent(name: "numberOfButtons", value: numberOfButtons , displayed: false)
    
    // Initialize default states
    numberOfButtons.times 
    {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
    }
    // These devices don't report regularly so they should only go OFFLINE when Hub is OFFLINE
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
    
}

def initialize() {
	state.lastButtonNumber = 0
 	configure()
}

def updated() 
{
   log.debug "updated() childDevices $childDevices"
   if (childDevices && device.label != state.oldLabel) 
   {
      childDevices.each 
      {
         def newLabel = getButtonName(channelNumber(it.deviceNetworkId))
	 	 it.setLabel(newLabel)
      }
      state.oldLabel = device.label
    }   
}
