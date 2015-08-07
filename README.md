JasperReports Server Shell (JRSH)
=================================
[![Join the chat at https://gitter.im/Jaspersoft/jrsh](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Jaspersoft/jrsh?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/Jaspersoft/jrsh.svg?branch=master)](https://travis-ci.org/Jaspersoft/jrsh) [![GitHub Release](https://img.shields.io/github/release/Jaspersoft/jrsh.svg)](https://github.com/Jaspersoft/jrsh/releases)
[![Stories in Ready](https://badge.waffle.io/Jaspersoft/jrsh.png?label=ready&title=Ready)](https://waffle.io/Jaspersoft/jrsh)

JRSH is a Command Line Interface for JasperReports Server. It's designed for fast and easy interaction with JasperReports Server.

Table of Contents
------------------
1. [Requirements](#requirements)
2. [Getting started](#getting-started)
3. [Frequently used operations](#frequently-used-operations)
4. [Specifying the file path on Windows](#specifying-the-file-path-on-windows)

## Requirements
Java >= 1.7 (Oracle or OpenJDK)

## Getting started
This short guide will walk you through getting a basic usage of JRSH in different modes, and demonstrate some simple operations.

First, you need to download a zip file with actual snapshot and unpack it. The snapshot folder contains the next content:
```bash
├── jrsh.jar
├── jrsh
└── jrsh.bat
```
Second, you need to execute a bash script `jrsh` with parameters. The parameters determine in which mode the application will work. There are three different modes:

1. Script Mode
2. Tool Mode
3. Shell Mode

In Script mode you should specify a script file with `*.jrs` file extension. That file contains an operation sequence. The sequenced operations are performed one by one. To run the script just execute in your terminal:

```bash
$> jrsh --script /Users/alex/jrsh/scripts/my_script.jrs
```
Here is a script file example:
```bash
#################
# my_script.jrs #
#################
login superuser%superuser@localhost:8080/jasperserver-pro
export /public/Samples
```

In the Tool mode an application executes only one operation. To switch it you must specify a connection string which follows the format:
```
[username]|[organization]%[password]@[url] (organization is optional)
```
See usage example example below.

```bash
$> jrsh superuser%superuser@localhost:8080/jasperserver-pro \ 
   import \
   /Users/alex/file.zip
```

And the last mode is `Shell`. This is an interactive mode. It executes your operation until you interrupt the app using Ctrl+C key. To switch to Shell mode all you need is tospecify a connection string.

```bash
$> jrsh superuser%superuser@localhost:8080/jasperserver-pro
```

## Frequently used operations

#### Import
This is most frequently used operation. It imports resources to [JRS](http://community.jaspersoft.com/project/jasperreports-server). You can import a zip file with resources or specify the folder with the unzipped resources to import.

![Import_Demo](http://i.imgur.com/Cusx7J6.gif)

`Import` examples:

Resources import
```bash
$> import /Users/alex/folder
```
Zip file import
```bash
$> import /Users/alex/archive.zip
```
Import resources with specifying import arguments
```bash
$> import /Users/alex/folder \ 
   with-update \ 
   with-skip-user-update \ 
   with-include-access-events
```

#### Export 
Export operation is used to export resources from the JRS. You can specify which resource you want to export, and it is also possible to specify where you want to save it. Similar to Import operation, you can specify the arguments of Export operation.

![Export_Demo](http://i.imgur.com/2UwfiqD.gif)

`Export` examples:

Export repository
```bash
$> export repository /public/Samples/Reports/RevenueDetailReport
```
Export repository with specifying export arguments
```bash
$> export repository /public/Samples/Reports/RevenueDetailReport \ 
   with-include-access-events with-user-roles
```
Export repository with specifying an export file
```bash
$> export repository /public/Samples/Reports/RevenueDetailReport \
   to \
   /Users/alex/jrs/downloads/export.zip
```

## Specifying the file path on Windows

Originally JRSH was created for Unix-like operation systems. Windows support was added later. So if your OS is Windows you should use a double backslash to separate subfolder and files. See example below:

```bash
$> import "D:\\Temp\\Jrsh\\import_archive.zip"
```

Here is the [link](https://www.student.cs.uwaterloo.ca/~cs132/Weekly/W03/FilePaths.html) with explanation why we should use double backslashes.
