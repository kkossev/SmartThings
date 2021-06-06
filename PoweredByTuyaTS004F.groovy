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
 *
 */

metadata {
  definition (name: "Powered by Tuya TS004F", namespace: "smartthings", author: "kkossev", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
	  capability "Refresh"
	  capability "Button"
	  capability "Health Check"
 	  fingerprint inClusters: "0000,0001,0003,0004,0006,1000", outClusters: "0019,000A,0003,0004,0005,0006,0008,1000", manufacturer: "_TZ3000_xabckq1v", model: "TS004F", deviceJoinName: "Powered by Tuya 4 Button TS004F", mnmn: "SmartThings", vid: "generic-4-button" 
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
    //
    if (descMap.clusterInt == 0x0008 && descMap.command == "01" && descMap.data[0] == "00" ) {
      buttonNumber = 3
      state.lastButtonNumber = 3
      buttonState = "held"
    }
    else if (descMap.clusterInt == 0x0008 && descMap.command == "01" && descMap.data[0] == "01" ) {
      buttonNumber = 4
      state.lastButtonNumber = 4
      buttonState = "held"
    }
    else if (descMap.clusterInt == 0x0006 && descMap.command == "00" ) {
    	buttonNumber = 1
        state.lastButtonNumber = 1
    	buttonState = "pushed"
    }
    else if (descMap.clusterInt == 0x0006 && descMap.command == "01" ) {
     	buttonNumber = 2
        state.lastButtonNumber = 2
    	buttonState = "pushed"
    }
    else if (descMap.clusterInt == 0x0008 && descMap.data[0] == "00" ) {
     	buttonNumber = 3
        state.lastButtonNumber = 3
    	buttonState = "pushed"
    }
    else if (descMap.clusterInt == 0x0008 && descMap.data[0] == "01" ) {
    	buttonNumber = 4
        state.lastButtonNumber = 4
    	buttonState = "pushed"
    }
    else if (descMap.clusterInt == 0x0008 && descMap.command == "03" ) {
      buttonNumber = state.lastButtonNumber
      buttonState = "up_hold"
    }
    
    if (buttonState in ["pushed","held","up_hold"] ) {
	    def descriptionText = "button $buttonNumber was $buttonState"
	    event = [name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true, displayed: true]
   	}
	if (event) {
	    log.info "Creating event: ${event}"
		  result = createEvent(event)
	} 
  } else {
	  log.warn "DID NOT PARSE MESSAGE for description : $description"
	  log.debug zigbee.parseDescriptionAsMap(description)
  }
  return result
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	log.debug "Configuring Reporting and Bindings."
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}

def installed() 
{
  	initialize()
    def numberOfButtons = 4
    sendEvent(name: "supportedButtonValues", value: ["pushed", "held", "up_hold"].encodeAsJSON(), displayed: false)
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
}

def updated() 
{
   log.debug "updated()"
}

