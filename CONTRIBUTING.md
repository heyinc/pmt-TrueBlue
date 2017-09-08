# Contributing

Constructive contributions to the project are very welcome, provided they meet certain requirements. This document details the various ways in which it is possible to contribute, and the requirements for each.

## Apparent Bug Reports

Please file an issue in the Github repository [here](https://github.com/Coiney/TrueBlue/issues).

The repository contains a default template which will be displayed when creating a new issue. Please follow the template to provide the required information. 

Please remember to be as detailed as possible when filing an issue. The more information we have, the more likely we are to be able to investigate and resolve it in a timely fashion.

Further, wherever possible, please include sample code which provides a Minimum, Complete, and Verifiable example demonstrating the exact issue is very welcome and greatly appreciated.

We strongly recommend that issues be filed in English. We will certainly do our best to respond to all issues regardless of language, but please understand that we may not be able to respond to issues filed in other languages in a timely fashion or even at all.


## Questions, Requests, etc.

Please file an issue in the Github repository [here](https://github.com/Coiney/TrueBlue/issues).

The repository contains a default template which will be displayed when creating a new issue. This template is designed primarily for the reporting of apparent bugs, so please do feel free to delete it and enter free text.

As per apparent bug reports, we would ask that you provide as much information as possible, and strongly recommend that issues be filed in English.


## Code (Bug fixes, enhancments, features, etc.)

We welcome any and all constructive code contributions to the project, provided they meet certain requirements.

We always appreciate an issue being created before code is submitted. This helps give us the background we need to do things like confirm issues and decide whether or not a feature is something that fits in with the roadmap for this repository.

If you'd like to contribute code you will need to file a pull request on the original repository. Pull requests must target the `develop` branch. Please see the "Branching" section below for more details on how to prepare and name branches for code changes.

Please understand that due to time constraints and the roadmap for this repository, we cannot guarantee that all contributions will be accepted.

### Code Style

We have not defined a strict code style for the project yet, so please follow what you see in the repository as best you can.

### Tests

Wherever reasonably possible, please add/update the tests appropriately as part of the code you submit as a PR.

### Javadoc

All public classes, interfaces, enums etc. and any methods on them which are not located under the `internal` package must have Javadoc describing their purpose.

As per our code style we have not yet defined a strict standard for Javadoc, so please follow what you see in the repository as best you can.

All documentation must be prepared in English. Please do let us know if you require assistance preparing the documentation - we are more than happy to help.

### Branching

We use [git flow](http://nvie.com/posts/a-successful-git-branching-model/) when working on this repository, and highly recommend that contributors do too.

When preparing a branch for contributing changes:

1. Make sure that the `develop` branch in your cloned repository is up to date with the `develop` branch in the original repository.
2. Cut a branch from `develop` in your cloned repository with a name fitting the pattern `feature/<description>`.

The description must be in English and should provide a clear, conscise summary of the content of and/or reason for the code change that will be made in the branch. Please aim to keep the description to no more than 30 characters.

When ready, raise a PR for the branch on the original repository with `develop` as the target branch.
