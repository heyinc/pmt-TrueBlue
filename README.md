# TrueBlue

### BETA WARNING

This library is current in beta. All APIs are subject to change without notice.


## Introduction

TrueBlue is a small wrapper service around the Android Bluetooth subsystem. It is designed to make 
interacting with Bluetooth on Android, which can be quite low level and frustrating at times, simpler.

In summary, its functionality includes:

1. Bluetooth adapter/subsystem management;
2. Bluetooth device pairing and connectivity management; and
3. Bluetooth discovery scanning.

For the sake of clarity, the service does *not* currently support:

* Bluetooth LE
* Bluetooth profiles
* Bluetooth device connectivity as a "server" (as opposed to as a client)


## Installation

#### Gradle

Coming soon ...


## Initialization

Before the service can be used it must be initialized with a `Context`. This need (and generally should) only be done once, so one good place to do this is in an `Application` subclass (if you have one). For example:

```
public final class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TrueBlue.init(getApplicationContext());
    }
}
```

Another good alternative is to initialize via a DI framework such as Dagger 2. See below for a very simple example:

```
@Singleton
@Module
public class MyModule {

    @Provides
    TrueBlue provideBluetoothService(Context context) {
        return TrueBlue.init(context);
    }
}
```

Once initialized, an instance of the service can be obtained for use as required using `TrueBlue.getInstance()`.

Be aware that it is always possible to initialize and obtain an instance of the service, even when no Bluetooth subsystem is available (e.g. emulators). In this situation methods pertaining to the availability and status of Bluetooth will work as expected, but the majority of other features will simply be no-ops.


## Usage

Please refer to the [project wiki](https://github.com/Coiney/TrueBlue/wiki/Usage) and Javadoc for 
more detailed information regarding usage.


## Test Application

The project contains a simple test application designed to exercise most of the features provided by the service. It is admittedly rather contrived and overly simple in certain places, but should at least provide a basic example of how the service can be used.


## Contributing

Please see [here](CONTRIBUTING.md) for detailed information on contributing to this project.

Additionally, a project [roadmap](ROADMAP.md) exists which details features we are currently considering implementing.


## License

```
Copyright 2017 Coiney, Inc.
Copyright 2016 - 2017 Daniel Carter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
