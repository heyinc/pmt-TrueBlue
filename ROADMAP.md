# Roadmap for TrueBlue

This file lists features which are planned for implementation or may be considered for implementation in the future.

### Bluetooth Profiles

Full support for these as per the Bluetooth guide. 

### Bluetooth LE

Full support for this as per the Bluetooth guide. 

### Bluetooth Server Support

Full support for this as per the Bluetooth guide. 

### Pairing

* Add a pairing (only) feature. This would be easy on APIs 19+ as the Android Bluetooth subsystem supports this, but on earlier versions we would either need to connect and disconnect (messy), or just connect and explain this in the documentation.
* Add a pairing event listener similar to the existing device connectivity listener.
* Add support for "unpairing" (removing bonds).

### Connecting

* Provide a means to obtain the instance of the class implementing the `Connection` interface for a given Bluetooth device which has already been connected to via the service after the fact. Currently this is only available via `ConnectionAttemptCallback` when the device is successfully connected to.
* Attempt to provide a means to prevent pairing with devices which are already paired with. Alternatively, permit this but provide a warning.

### Connections

* Decide whether the asynchronous connection client write error and write success callbacks should return the written data or not.
* Potentially limit the amount of data which can be written via the asynchronous connection client to prevent the queues getting too large.
* Add metadata such as the dates and times connections are established.
* Introduce additional monitoring to ensure that connection tasks and so on never get stuck.

### General

* Consider simplifying concurrency by using `HandlerThread` behind the scenes instead of `Thread` and `ExecutorService`. A few things to consider:
  * This approach could be difficult for certain tasks such as connecting to Bluetooth devices, which is very complex. Mind you, that very complexity could be at least in part due to the current approach to concurrency, and in any event suggests that there is probably a better way.
  * The impact on the service interface - in particular, on what is synchronous and what is asynchronous. Methods like `isDeviceConnected` are currently synchronous, and to retain that approach we would probably still need to use synchronisation behind the scenes. The alternative is to go asynchronous, but this seems like it might be overkill (and frustrating?) for simpler tasks.

### Tests

* Improve and expand test coverage.
