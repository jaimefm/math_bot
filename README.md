# MathBot

#### Learn counting to calculus by programming a robot!

### How the project is funded
Mathbot is a 100% volunteer funded project.
Contributors are not paid, 
but their efforts are tracked
and if tips are provided to mathbot
those tips are distributed to contributors.

Playing all of the levels on mathbot
will always be free for everyone around the world
and it is an open source project
so anyone is free to copy all of the work
we have done to date as their own starting point
for building a competing application.

We want excellent competitors because we want
kids to be rescued from the mental abuse
and discouragement they often suffer at the hands
of poor quality math teachers.

In the coming months we also plan to release
a premium experience that will allow
parents, grandparents or anyone else
to reward kids with bitcoin for mastering 
programming concepts.

While we already encourage parents
to provide additional incentives and
rewards for completing levels
we think the automatic and immediate
in game rewards will be even better
at helping to make math and programming 
an enjoyable experience.

For example you might buy "Algebra" for $50
for a child or grandchild.
In that case the student will be rewarded with $45,
at maybe $3 per level,
for learning Algebra and passing the 15 levels.

The remaining $5 will be distributed to 
past volunteers as a tip.

For more details on how tips are distributed
to contributors see:
https://github.com/JWWeatherman/tipping_tokens

## Running The App

### Prerequisites

  - Java -v 1.8
    - [Java](https://java.com/en/download/)
  - Scala -v 2.11.11
    - [Scala](https://www.scala-lang.org/download/)
  - NodeJS -v 7.10.1
    - Use [N](https://github.com/tj/n)
  - WebPack installed globally
    - ```npm install webpack -g```
  - MongoDB install globally
    - Goto [MongoDb](https://docs.mongodb.com/manual/installation/)
    
### Suggested IDE

The project has been built thus far with Intellij
   - [Intellij](https://www.jetbrains.com/idea/) 
     - Plugins
        - Scala Plugin
        - VueJs Plugin
        - ScalaFmt Plugin 

### Getting started

#### Github and Google api keys
You will need keys and secrets for these services

- Google API w/Google+ enabled
- Github
- SendGrid

Add the following to your environment, then restart your OS
```
 mathbot_oauth_google_clientId="your_id"
 mathbot_oauth_google_clientSecret="your_secret"
 mathbot_oauth_github_clientId="your_id"
 mathbot_oauth_github_clientSecret="your_secret"
 mathbot_admin_custodianEmail="your_email"
 mathbot_sendgrid_secretKey="your_sendgrid_secret"
```

FYI - if you are using Intellij be sure to do a full restart of your system after you set theses env variables.

#### Starting and build app

* This application is not using any of the Scala play views and all the views are served by the [Vue](https://vuejs.org/) code base which is inside the `ui` folder.

* Use any of the sbt commands listed in the below according to the requirement which are working fine with this application.(To see more details of [sbt](http://www.scala-sbt.org/))

``` 
    sbt clean               # Clear existing build files
    
    sbt stage               # Build your application from your project’s source directory 
                     
    sbt run                 # Run both backend and frontend builds in watch mode
    
    sbt dist                # Build both backend and frontend sources into a single distribution
               
    !! sbt test             # Run both backend and frontend unit tests !! *coming soon!  
```

### Game Rules to Pass A Step

1) Program must be completely finished running
2) Robot must not be carrying anything
3) Robot must be standing on the portal
4) Correct amount of items must be placed on portal (if problem exists)
5) Special parameters must be met (if any)

### Implementing New Levels

* Rules
    * Levels are located in `conf/assets`
    * Levels and steps are a bi-direction linked list data structure
    * The users stats will be automatically updated in the database when a change to the levels is detected
    * Each level must have at least a single step

#### Adding a level

1) Add a file to the assets directory
    - File name must match the level name
    
2) Copy this json into the file
```
{
  "level": "<{string}LEVEL NAME (MUST MATCH FILE NAME)>",
  "prevLevel": "<{string}PREVIOUS LEVEL NAME (IF NONE TYPE "None">",
  "nextLevel": "<{string}NEXT LEVEL NAME (IF NONE TYPE "None">",
  "show": <{boolean}(If the level's previous is "None" this field must be true)>, 
  "steps": {}
}
```

3) Fill out the json

#### Adding steps to the level

1) Add this json to the `"steps"` field 

```$xslt
 "<NAME OF STEP (must start with a capital and be camel cased)>": {
      "level": "<{string}LEVEL NAME (must match the level name exactly)>",
      "step": "<{string}MUST MACH NAME OF STEP EXACTLY>",
      "gridMap": <{array[string]}SEE `Creating a Grid Map` SECTION>,
      "description": <{string}SEE `Creating a Description` SECTION>, 
      "mainMax": <{int}<ALLOWABLE COMMANDS IN MAIN (if infinite -1)>,
      "robotOrientation": <{int}ROBOTS STARTING ORIENTATION (0, 90, 180, 270 ~ only)>,
      "stagedEnabled": <{boolean}INDICATES IF THE USER IS ABLE TO OPEN THE STAGED FUNCTION POPOVER>,
      "activeEnabled": <{boolean}INDICATES IF THE USER CAN ADD ANYTHING TO ACTIVE FUNCTIONS (almost always true)>,
      "stagedQty": <{int}QTY OF STAGED FUNCTIONS AVAILABLE (if infinite -1)>,
      "assignedStaged": <{list[object]}SEE `Adding Assigned Staged` SECTION>,
      "preBuiltActive": <{list[object]}SEE `Adding Pre-Built Actives` SECTION>,
      "allowedActives": <{list[string]}LIST OF FUNCTION CREATED IDS (only applies to built functions)>,
      "cmdsAvailable": <{array[string]}ARRAY OF COMMAND NAMES, SEE `Command Names` SECTION FOR COMMAND NAMES>,
      "specialParameters": <{array[string]}LIST OF SPECIAL PARAMETERS, SEE `Special Parameters` SECTION FOR CURRENT SPECIAL PARAMETERS>,
      "problem": <{string}SEE `Building a Problem` SECTION FOR PROPER PROBLEM SYNTAX,
      "clearMain": <{boolean>INDICATES IF MAIN SHOULD BE EMPTY AT THE START OF THE LEVEL>,
      "initFocus": <{array[string]}LIST OF ELEMENTS TO ADD SPECIAL EFFECT ON AT START OF STEP, SEE `Init Focus` SECTION>
      "videoHints": <{array[string]}LIST OF YOUTUBE VIDEO IDS>,
      "freeHint": <{option[string]}YOUTUBE VIDEO ID THAT PLAYS AT THE BEGGINING OF LEVEL AUTOMATICALLY>,
      "prevStep": <{String}PREVIOUS STEP NAME (Should be "None" if first step)>,
      "nextStep": <{String}NEXT STEP NAME (Should be "None" if last step)>
    },
```

#### Command Names 
* Command names can also be found in `app/model/DefaultCommands.scala`

```$xslt
  "changeRobotDirection"
  "moveRobotForwardOneSpot"
  "setItemDown"
  "pickUpItem"
```

#### Creating a Grid Map
* Legend
  
```$xslt
(R) - Robot Spot
($) - Portal
|E|	- Robot can go
|W|	- Robot can't go
[1]	- Self replenishing - value 1
[10] - Self replenishing - value 10
[100] - Self replenishing - value 100
[1g] - Self replenishing - value 1,000
[10g] - Self replenishing - value 10,000
[TS(<TOOL>,<TOOL>)] - None self replenishing tools (comma sperated, no spaces) - e.g [TS(1,10,100,1g,10g)]
```
* Grid map creation
    * Use the above legend to fill in the grid map
    * We have only extensively tested a 5 x 10 grid however the grid can be any dimension! 
```$xslt
[
    "[1]   |E| |E| |E| |E| |E| |E| |E| |E| |E|",
    "[10]  |E| |E| |E| |E| |E| |W| |W| |W| |W|",
    "[100] |E| |E| |E| |E| |E| |W| |E| |E| |E|",
    "[1g]  |E| |E| |E| |E| ($) |W| |E| |E| |E|",
    "[10g] |E| |E| |E| |E| (R) |W| |E| |E| |E|"
]
```

#### Creating a Description
* Include the following in the description to be converted 
```$xslt
!!P!! - includes built problem in description
!!L!! - includes level name in description
!!S!! - includes step name in description
!![img]src='<IMG SRC (Must be in single quotes)>'!! - include an image in description, will be same size as font
```

#### Adding Assigned Staged
* Adds specified staged functions to staged functions
1) Add image resource url in `ui/src/assets/assets.js` to `funcImages`
    
2) Add assigned staged object to JSON scala case class located ar `app/actors/messages/AssignedFunctionModel.scala`
```$xslt
[
    {
        "name": <{string}DISPLAY NAME>,
        "image": <{string}NAME OF IMAGE IN ASSETS>,
        "createdId": <{string}CREATED ID, 11 DIGIT STRING OF NUMBERS STARTING WITH 5 (generate using random.org)>,
        "sizeLimit": <{int}MAX SIZE FUNCTION CAN BE>,
        "func": <{list[string]}MUST BE AN EMPTY ARRAY>,
    }
]
```

#### Special Parameters 
* These are special requirements beyond the regular requirements (see `Game Rules to Pass a Step`) to pass this step

Current Special Parameters
```$xslt
    "functionRequired" - user must use a built function
    "recursionRequired" - user must use recursion (currently only checks top level)
```

#### Building a Problem
* {int} - becomes an integer with that many digits
* int - becomes self
```$xslt
"{1} + {2} * ({6} - {4}) - 55" == "4 + 65 * (123456 - 4513) - 55"
```

#### Adding Pre-built Actives;
* Active functions already built for the user
```$xslt
[
    {
        "name": <{string}DISPLAY NAME>,
        "image": <{string}NAME OF IMAGE IN ASSETS>,
        "createdId": <{string}CREATED ID, 11 DIGIT STRING OF NUMBERS STARTING WITH 5 (generate using random.org)>,
        "sizeLimit": <{int}MAX SIZE FUNCTION CAN BE>,
        "func": <{list[string]}LIST OF COMMANDS (see 'command names' section}>,
    }
]
```

#### Init Focus
* Elements to alert the user of when the step loads
* Elements will flash twice in the order they are found in the array
* Helpful to demonstrate what order things must happen to pass the level
* Name becomes the corresponding id on the front end either by becoming the hashed value, or literal
```$xslt
    # Commands - will be hashed into the corresponding id
        > changeRobotDirection
        > moveRobotForwardOneSpot
        > setItemDown
        > pickUpItem
    # Functions - will be hashed into the corresponding id
        > Use the functions `name`
    # Other elements - will be converted into the correct id name
        > main-placeholder (only visible when main max set)
        > staged (green + that opens staged functions)
```


