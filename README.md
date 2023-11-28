# TeamCity plugin - Unreal Engine BuildGraph Runner

## Overview
This plugin is a nicer interface for running the Unreal Automation Tool (UAT) with the 'BuildGraph' command.  Once you have specified the path to your BuildGraph XML file in source control, the plugin will parse it and offer suggestions for both the name of the Target Node to build and for any Options you want to pass in.

When the job runs, the agent will (optionally) re-format any errors and warnings to use the correct markup for TeamCity to be able to recognise them.  This means that options like 'Important Messages Only' in the Log view for a build are more reliable.  It also tries to make the log more hierarchical, putting each Build Graph Node in to its own section, and separating tool invocations where it sees them.

This information is also used to:
 * Append the number of Errors and Warnings (if any) to the 'Build Status' string
 * Insert a error/warning summary to the Build Results Overview tab (in the modern UI this is also in the 'build line' that expands on the build page), breaking down the errors and warnings by build phase (Build, Cook, Stage, Package etc)
 * Add a Build Graph Report tab to the Build Results page with a table of errors and warnings
 * Create a downloadable XLSX artifact with all errors and warnings.  Apache POI is used for this.
 * On builds that include Cook, the Cook Stats portion of the log is also parsed and many parts of is are displayed on the Cook Report tab.  This has three sub-tabs:
   * Cook Stats - these are the stats that appear as 'key-value' pairs in the output
   * Hierarchical Stats - there are two sections that have hierarchical timer information about the cook itself
   * Sequenced Stats - if the cook is outputting regular information such as the cooked/remaining numbers, or memory/file handle usage, the plugin will draw a graph of these values against the build time

### Unreal Game Sync
When using the runner, it is also possible to send Starting/Success/Failure messages to a Unreal Game Sync metadata server.  In the Admin page of a project, select the Unreal Game Sync tab and enter the URL of the server.  Then in the runner you can specify the badge name, along with the Project path.
## Finally
Hope it's useful for someone else.  It's my first time writing Kotlin and my first TeamCity plugin, and I'm sure there are many improvements that could be made to it.  It'd be great to hear about any changes you make and pull requests are very welcome.

## Acknowledgements
This could not have been written without reference to many of the TeamCity plugins that have been published on Github.  While I don't think that beyond the general structure of the folder and the docker-compose setup that came from the Sakura UI sample project that I've used any source files directly, I've definitely referred to many of them for ideas and inspiration while writing this.  So thank you very much.

