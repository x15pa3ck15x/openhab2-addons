# Domintell Binding

This binding integrates [Domintell](https://www.domintell.com/en/) home automation system and openHAB using Light protocol provided by DETH02 ethernet communication module. 

## Supported Things

Current version of the binding supports the following things (Domintell modules):

* **DBIR01** - Output module with 8 bipolar relays. **BIR** thing type supports 8 switch channels.
* **DMR01** - Output module with 5 monopolar relays. **DMR** thing type supports 5 switch channels.
* **DISM08/DISM04** - Input module with 8/4 free of potential contacts. **IS8/IS4** thing types support 8/4 contact type channels (OPEN/CLOSE) and one command channel with the following string commands:
  * SHORT_PUSH-[1..8] to simulate short push events
  * LONG_PUSH-[1..8] to simulate long push events
* **DTEM01** - Temperature module. **TE1** thing type support thermostat channel group with the following channels:
  * **presetValue** - Preset temperature (RW)
  * **currentValue** - Measured temperature (RO)
  * **presetValue** - Profile temperature configured in Domintell system (RO)
  * **mode** - Regulation mode [AUTO|ABSENCE|COMFORT|FROST] (RW)

## Discovery

Binding supports full auto-discovery feature. The bridge discovery is initiated after bundle start and adds a bridge to the inbox.
After configuring the host/port for bridge the binding will try to connect to Domintell DETH02 module to discover the Domintell full installation.
All supported modules listed above will be added to the inbox when the first status message arrives from the module.

## Thing Configuration

The only required configuration for this binding are the bridge level **host** and **port** parameters. For this the Domintell DETH02 should use

* Static IP address
* Session timeout should be set at least to 2 min
* Exclusive session should be false
* No NTP is needed
* Clear the password

**Note:** For thing IDs always use the hexadecimal address of the Domintell module!

Bridge level host/port can be configured using PaperUI or it can be set in the things file as well:

``
Bridge domintell:bridge:DETH02 "Domintell Bridge" [address="10.200.0.6", port=17481]
``

## Full Example

### .things
    
    Bridge domintell:bridge:DETH02 "Domintell Bridge" \[address="10.200.0.6", port=17481\] {
        //Thermostats
        Thing te1 AB9 "Thermostate AB9/2745"    //groundfloor
        Thing te1 C8A "Thermostate C8A/3258"    //first floor
    
        //Relays
        Thing bir 2C37 "Relay 2C37/11319"

        //Contacts
        Thing is8 36D3 "Input module 36D3/14035" [inverterMask="11010100"]
    
        //Variables 
        Thing var F "Irrigation" //Enabled=1
    }

### .items 

    //temperature
    Number domGroundFloorTemp "Ground floor temperature" {channel="domintell:te1:DETH02:AB9:thermostate#currentValue"} 
    Number domFirstFloorTemp "First floor temperature" {channel="domintell:te1:DETH02:C8A:thermostate#currentValue"}
    
    //relays
    Switch lPantry "Pantry light" {channel="domintell:bir:DETH02:2C37:output-4"}   
    Switch lKitchen "Kitchen light" {channel="domintell:bir:DETH02:2C37:output-8"}

    //contacts
    Contact msKitchen "Kitchen motion" {channel="domintell:is8:DETH02:36D3:contact-5"}    
    Contact msSmallBathroom "Small bathroom motion" {channel="domintell:is8:DETH02:36D3:contact-3"}    
