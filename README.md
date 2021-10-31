# SmartThings

My experimental SmartThings Device Handler for the new 4 button Scene Switches 'Powered by Tuya'
When these new 'Powered by Tuya'scene swicthes are used paired to a Tiya zigbee hub, all works perfect... but only within Tuya cloud platform. :( 

Tested in SmartThings platform using ABC Manager app.

Rev 1.1 : - added 'held' and 'up_hold' (release) events decoding for the 2 right buttons (3 and 4). Works only when paired initally to Tuya Zigbee hub annd the battery is not removed!

Rev 2.0 : - **shoud work** in SmartThings, bringing all 12 possible buttons combination without pairing to Tuya /or Hubitat:)/ hubs first!
          - when pairing to ST hub, TS004F is initialized in 'Scene Control' mode. Note, that you need to first REMOVE the device that uses other driver and then pair the device
            again from SmartThings mobile app!. The initialization in Scene Control mode happens only when the device is in pairing mode, hence it is not possible to simply
            change the driver... the device must be removed and then added to ST hub again!
          - simulteniously pressing buttons 2 & 3 for more than 5 seconds switches between Dimmer and Scene modes. LED 1 should blink shortly.
          - 4 child devices are created in order to use the standard SmartThings button capabilities (code by SangBoy)

For autmations withing SmartThings hub please use https://github.com/YooSangBeom/SangBoyST/blob/master/devicetypes/sangboy/zemismart-button.src/zemismart-button.groovy 

