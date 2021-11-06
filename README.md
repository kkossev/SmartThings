# SmartThings

My experimental SmartThings Device Handler for the new 4 button Scene Switches 'Powered by Tuya'
When these new 'Powered by Tuya'scene swicthes are used paired to a Tiya zigbee hub, all works perfect... but only within Tuya cloud platform. :( 

This DH is based on https://github.com/YooSangBeom/SangBoyST/blob/master/devicetypes/sangboy/zemismart-button.src/zemismart-button.groovy and on a lot of research for the same issue in other home automation platforms.

Tested in SmartThings platform using ABC Manager app and simple automations

Rev 1.1 : - added 'held' and 'up_hold' (release) events decoding for the 2 right buttons (3 and 4). Works only when paired initally to Tuya Zigbee hub annd the battery is not removed!

Rev 2.0 : - **shoud work** in SmartThings, bringing all 12 possible buttons combination without pairing to Tuya /or Hubitat:)/ hubs first!
          - when pairing to ST hub, TS004F is initialized in 'Scene Control' mode. Note, that you need to first REMOVE the device that uses other driver and then pair the device
            again from SmartThings mobile app!. The initialization in Scene Control mode happens only when the device is in pairing mode, hence it is not possible to simply
            change the driver... the device must be removed and then added to ST hub again!
          - 4 child devices are created in order to use the standard SmartThings button capabilities (code by SangBoy)
          
Rev 2.1   - optimized configuration; removed reverseButton settings; debug logging is now true by default
Rev 2.2   - ... and one more (initialization) for luck!
Rev 2.3   - ... and initialize again on every Dimmer Mode event! (hopefully happens just once)

