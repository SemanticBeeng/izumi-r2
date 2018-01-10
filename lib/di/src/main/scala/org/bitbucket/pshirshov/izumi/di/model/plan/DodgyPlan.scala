package org.bitbucket.pshirshov.izumi.di.model.plan

import org.bitbucket.pshirshov.izumi.di.model.plan.DodgyOp.{Nop, Statement}

/**
  * ***********,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,***./((%%&&&%%##((//////((((((((((((((((((((((///(((((((((//////(((
***************,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,***./((%%%&%###((////////(((((((((((((((((((((//(((((##############
*.////8**************,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,**./((%%(*,***********./////////////////////////(((#%%%%%%%%%%%%%%
**./////////8*********,,,,,,,,,,,,,.,,,,,,,,,,,,,,,,,**.//(/.,***************.////////8*././/////.////(((%%&&&&&&&&&&&&
****.////////////////8***,,,,,,,,,,,.....,,,,,,,,,,,,**./8,.,*.//////8**,./8.//////////8********.//////((#%&&&&&&&&&&&&
*****./////////////////8***,,,,,,,,,,......,,,,,,,,,,****. .,*.//////8***./////((((((/////////////////(((#%&&&&&&&&&&&&
*./8**.////////////////////8***,,,,,,,,,......,,,,,,,,,**,...,*.//////8***,,.//((((((((///////(///////((((#%%%&&&&&&&&&&&
*(/////////////////////////8***,,,,,,,,......,,,,,,,,,,. .,,.////////8**,,,.///((((((((/////////((///(((##%&&&&&&&&&&&&&
*#####((((((((////////////////8**,,,,,,.......,,,,,,,. ..,*.////////8**,./8,*.////////////////////////((##%&&&&&&&&&&&&&
*##########(((((((////////(////8**,,,,.......,,,,,,,, ..,**.////////8***./8,**.////////////////.///////((#%&&&&&&&&&&&&&
*((############((((((((/(((((////8**,,.......,,,,,,, ...***.////////8**,.//,*./////((/////////////////(((#%&&&&&&&&&&&&&
****.//(((#####((((((((((((((///8*,,,.....,,,,,,,.  ..****.////////8***./8,.///(((((((((((((///////(((((#%%%&&&&&&&&&&&
*,*****./////((((((((((////////8***,,,,,,,,,,,,,,,   .,***./////////8**,***,.///((((((((((((((((((((((((((##############
*.////////////////////////////////8***************.  .,***.//////////8**,***.////////////////(((((((((////(((((((((((((((
*((((((((((((/((((((((((((((((((((((/(/////////8.  ..***.///////////8**,***.////////////////////////////////////////////
*############################################(*   ..,**.////////////8**,***((((((((((((((///////////////////////////////
*%%%%%%%%%%%%%%%%%%%%%%%%&&&&&&%&&&%&&&%%%%%%/   .,,,///////////////8***./#################(((((((((((((((((((//////////
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%     ..,.//(//////////8*****(&&&&&&&&&&&&&&&%%%%%%%%%%%%###########((((((((
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%,    ..,*./////////////8****,(&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%%%%%%%
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%*   ..,,*.//////////////8**,,./%&&&&&&&&&&&&&&@&&@&&@@&&@@@@@@@@@@@@@@@@@@@@@
*&&&%&&&&&&&&%&&&&&&&&&&&&&&&&&&&&&&&&&&&%/    ..,**./////////////8**,,***#&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&@&@@@@@@@@@@@@
*&%&&&&&&&&&&&&&&&%&&&&&&&&&&&&&&&&&&&&&.   ..,**.//////////////8*****,,(&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&@@@@&&@
*%&%&&&&&&%&&%&%%&%%%%&%&&&&&&&&&&&%%%%%%.   ..,,*.///////////////8***,**,(&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&@@@@
*&%%&&&&%&&&&%%%%%%%%%&%%&%%%%%%%%%(,,........,,,,*******.///////8****,,,,/&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&@&&&&
*%%%%%&%%%%%%%%%%%%%%%%%%%%%%%%%%%*      .,,,*****.//////8************,,,,/&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
*%%&%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*     .,*.//////(((((((((/////8***,,,,,,/%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&(.   ..,.//((((((((((((((((((/////8,.  .,,*(%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%,   ..,.//(((((((######(((((((////8,,..,,./%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&. ..,,*./(((((#########((((((((///8**,,**(%&&&&&&&&&&&&&&&&&%%%%%&&&&&&&&&&&&&&&&&&&
*&&&&&&&&%%%%%%%%%%%%%%%%&&&&&&&&&%%/. ..,,,*((((#############(((((////8***./#%&&&&%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%&
*&%%%%%%%%%%%#%%%#%#####%%%%%%%%%%%%%/..,*.//((((((########((((((/////8.///(#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%###########%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%#%%%%##    .,,./((###########(((((((//////(((//8****,*#%%%%%%%%%%%%%%%%%%###############%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%###(.../8***.//((((##%%&&&&&&&%(*.,,,*.//////////8,*#%%%%%%%%%%%%%%%##################
*%%%%%%%%%%%%%%%%%%%##############%###. *(((/8.//(((#%&@@@@@&&&%(/8,,,,**.//////////8,*(##%%%%%%%%%%%########%%%#######(
*%%%%%%%%%%%%%%%##((///8*******.////((,  .(##/.//((%&@@&&@&((/8**,...,*./////////8*.   ../((####%%%%#%%%%%%%###((/////
*#%%%%%%%%%%%%%%##((//8******,,,,*****,...,****./((#&%%%###(((//8**, ..,*./////////8*.     .,.///((##%%%%##%####(//8****
*##%%%%%%%%%%%%%%%###((///8***********, ..,,***,.//(((((((((##((/8,,...,*./////////8,.      .,,**./((####%######(/8*****
*##%%%%%%%%%%%%%%%%%%%%###(((((////////(#*.,,**,.////(((#%&&&&(/8,,..,*.////////8*,,.     ..,**.//(####%%######((/////
*%#%%%%%%%%%%%%%%%%%%%%%%%%########((((/,,...,,.,.////((#%&&@@&/8,,..**./////////8,*,.   ..,,./(((#####%%##%#%%#######
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%##,    ....,*.///((#%&&@@/8**,.,*./////////8****...,,,,*(#####%%%%%%%%%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%,      ...,**.///(((##((//8**,,,,*./////////////8*****,,/%%%%%%%%%%%%%%%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%.      ..,,**.////((/////8***,,,*.///////////////////,,/#%%%%%%%%%%%%%%%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/      .,,,**.//////(/////8***,,*.///(((/////////////8*./(%%%%%%%%%%%%%%%%%%%%%%%%
*%%%%%%%%%%&%%%%%%%%%%%%%%%%%&%%%%%%%%#.    ..,,,***./8*.//(/////8***,*.//((((//./////((((/8****./%%%%%%%%%%%%%%%%%%%%%%
*%%%%%%%&%%%%&&&%%&%%%%%%%%%%%%%%%%%%%%/.   .,,,**./////8./(/////.//8,,.///(((/8*.////((((/8*****./#%%%%%%%%%%%%%%%%%%%%
*%%%%%%%%%%&&&&&&&%&&&&&%&&%%&&%%%&%%%%%*  ..,,,,,.///////(/////8.//8***.//(/8*.////(((((/8.//8***,,/%%%%%%%%%%%%%%%%%%%
*%%&%%%%&&&&&&&&&&&&&%&%&&&%&&&&&%%%&%%(. ..,,,,,,.///(((//////8*.//8***./(/8**.////((((//.////8**,,*(#%%%&%%%%%%%%%%%%%
*%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%&%%,  ..,,,,,**.//////////.////8***.//(/.//////(((((////////((///8****#%&%%%%%%%%%%%
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%#.  .,,,,,,***./////////////8****.///////////(((((//((/(((#(///////8**(%&&&&&%%%%%
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%/..,,,...,,**.////////////8***.///////////(((((((((((((#%#(((//////8***#%&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%(.    ..,,,,.////////////8**.///(((((//((((((((((((((##%#((((//////8***#&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%*     ..,,,,***.////////8**.///((((((((((((##(((((((##%##((((((/////8*./%&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%*    .....,,,**.////////8*.//./(((((((##((#(####((####%##(((((((/////8*,*#&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&(.  ..,....,,,*.///////8*.///8./((((((################%%##(((((((((///8..,#&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&,   .,,,...,,,*.//////8******.//(((((################%%#(((((((((((//8,  ,#&%&&%%%
*@@@@@@&@@@@@@@@@@@@@@@@@@@@@@@@@@@&&%   ..,,,,...,*.//////8*./((/.///((((((((####(#######%%%#(((((((((((//8. .*#%%%%%%%
*@@@@@@@@@@@&&&&&&&@&&&&&&&&&&&&&&&&%*  ..,,,****,,*.///8*..*./((((((((((((((((#((((#####%%%(((((((((((((/8,..,/%%%%%%%%
*&&&&&&&&&&&&&&&&&&%%%%%%%%%%%%%%%%%/  ..,,,,*.//(###((///8.,./((((((((((((((((#((((#####%%#(((((((((((((/8,,,*#%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%(.  .,,,,*.//((#####((//8,.//(((((((((((((((#((((####%%#(((((((((((((//8***(#%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%#*  .,,,,**.//(((((###(((///////(//((((((((((#(((#####%#((((((((((((((//8*./#%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%#*  ..,,,**.///((((#%%##((((((((((((((((((((((#(((###%##/8*.///((((((((////(#%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%(.  .,,****.//((//#%%%%##((((((((((//((((((((###(###%%/...,*.//((((((/(////#%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%(.  ..,,***.//////(%%%%%###(((((((((((((((((((########*....,.///(((((((((/(#%%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%#,  ..,,***.//////(#%%%%%(((#((((((((((((((#((#######(, ...,.////((/(((((//#%%%%%%%%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*  ..,,*********./(#%%%%%(**(#((((((#(((((##########*....,,.///////((((((((%%%%%%%&%%%%%%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%#  ..,,,,,*.///8**./////(##*,/##(((((#((############*....,*.///////((((((((%%%%&&%&&&&&&&%
*%%%%%%%%%%%%%%%%%%%%%%%%%%%%#*  ...,.///////((//8./////((//((##(((#############/. ...,*.////////(((((//(%&&&&&&&&&&&&&&
*%%%%%%&&&%%%&&%&%&%%%%%%%%%#,  .,./////(((////./((####/8((((((###############(.  ..,*.////////((((((/8./%&&&&&&&&&&&&&&
*&%%%&&&%&%&&&&&%&%%&&&&&%%%* .,./////((////8**#%%%%%##(/(###################,.....,.///////////(((///8./%&&&&&&&&&&&&&&
*%&&&&&&&&&&&&&&&&&&&&&&&&%*.../////((((((#%%#./#%%%%%%#(#%%%%%############*. ...,*.////////////((////8,*#&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&%*,.////(((((##%%%%%%(./%%%%%(%&%&&%(##########*....,**.////////////((((((/8,*#&@@@@@@@@@@@@@
*&&&&&&&&&&&&&&&&&&&&&&%/,./////(((##%%%%%%%%%#/8(%&&&(%&&&#%%#(####/8****./////////////((((((#((/8**(@@@@@@@@@@@@@@
*&&&&&&&&&&&&&&&&&&&&&%*,./////((((#%&&%%%%%%%%(/(%&&&#%&&&%&&%(##(////////////////////((((((###((/8,/@@@@@@@@@@@@@@
*&&&&&&&&&&&&&&&&&&&&%/.///((((##/8./#%&%%%%&%%%//%&&&#&&&&%#&&&%#((///////((////////////(///(####((/8**&@@@@@@@@@@@@@
*&&&&&&&&&&&&&&&&&&,,///(((###%%%(/./(%%%&%%%%(/%&&&%#%&&@%&&%#((//////((((((////(((((((//(#%###((/8**%@@&&&&&&&&&&&
*&&&&&&&&&&&&&&@&&%*,.//(((###%%&&&&%#/8*(%&&%&(#%&&%#&@&@%%&%#(((((/((((((((/////(((((((((((######(/8**(&&&&&&&&&&&&&
*@@@@@@@@@@@@@@@@%*,.//((###%%%&&&&&&&%#/8(%%&&(#%&%&@@&%##((((((/(((((/(((////((((((((((########((/8./%&&&&&&&&&&&&
*@@@@@@@@@@@@@@@&*,.//((###%%%&&&&&&&&&/8((%&(#&&%%%&&%#(((((((((((((((/(((((((((((((((##########((/8./#&&&&&&&&&&&&
*@@@@@@@@@@@@@@@,..//((###%%&&&&&&&&&%%&//(%&##&@%%%%#(((((((((((((((((((((((((((((((((######%###((/8**(&&&&&&&&&&&&
*@@@@@@@@@@@@@&*..//(###%%%&&&&%%%%&&&&&&((%&#%@##((((((//(((((((((((((((((((((((((#%%#####%####((/8./#&&&&&&&&&&&
*@@@@@@@@@@@&&/.*(((##%%%%&&%%%%%%&&&%#&&&(#&%%%&%###(((((((((((((((((((((((((((((((((#%%####(#%####((/8**(%&&&&&&&&&&
*&&&&&&&&&&&&(..(((###%#((//////////(#/(%&(##%%&&%%###((((((((((((((((((((((((((((((((#%%%#####%####((//8./%&&&&&&&&&&
*&&&&&&&&&&,,/(###%%#####%%%%%%#(///(((#%##%&&&@&%%%####((((((((((((((((((((((((((((###%%%######%####((/8./(%&&&&&&&&&
*&&&&&&&&&&%/./((###%%%%%&&&&&&%%&&&%#((((#%%%&&@&%#########(((((((((((((((((((((((((####%%%######%####((/8.//#&&&&&&&&&
*&&&&&&&&&&(./((##%%%%%%&&&&&&&&&&&&&&%##%#%%%%#((((((###%###(((((((((((((((((((((((#####%%%%#####%####((/8.//(#&&&&&&&&
*&&&&&&&&&%/./((#%%%%%&&&&&&&&&%%%&&&&%%%%&%#((///8*./((##%%####(((((((((((((((((((#%%###%%%%#####%#####(/8*.//(%&&&&&&&
*&&&&&&&&%/./((#%%%%%&&&&&&%%%%%%######%&&&%#((//8,.,.//((#%%%####(((((((/((((((((#%%%###%%%%#####%%####((/8.//((%%&&&&&
*&&&&&&&./((##%%%%%&&&&%%#((((((((##%&&&&%%##((////////((#%%%####((((((((((((((#%%%%%##%%%%#####%%####((/8.///(#&&&&&&
*&&&&&&&%/./(###%%%%&&&%#(///(#%%%&%%%&&&@&&%%%####((/////((%%%%####((((((((((((#%%%%%%##%%%%#####%%%###((/8.///((%&&&&&
*&&&&&&./((##%%%%&&%#///#%%&&&&&&&%&&&@&%%%%#((((////////(#%%%####(((((((((((#%%%%%%%##%%%%%####%%#####((/8.///(#&&&&&
*&&&&&&&/./(##%%%%%%(//#%&&&&&&&%%#%%&&@&%#(((//8,,.//(////(#%%%%####((((((((#%%%%%%%%%%%%%%%%####%%%####((/8*.//((%&&&&
*&&&&&&&//((##%%%#(/(#%&&&&&&%#####%&&&&&%#((//8*.///(((((/(##%%%%####((((((#%%%%%%%%%%%%%%%%%%###%%%#####((/8.//((%&&&&
*&&&&&&%//(##%%%#(#%&&&&&&&&&%###%&&&&&&&&%##(////(((//((((/(#%&%%%####((((#%%%%%%%%%%%%%%%%%%%###%%%#####((/.///(#%&&&&
*&&&&&/(##%%%%%&&&&&&&&&&%###&&@&%&&@@&@&%%#(((((///(((((/(%&&&%%%###(((#%%%%%%%%%%%%%%%%%%%%###%%%%####(((////(#%&&&&
*&&&&&/(#%%%%%&&&&&&&&%#(#%&@@&%%%&&@@@@&%%%#((//./((((//(%&&&&&%%#####%%%%%%%%&%%%%%%%%%%%%%%##%%%%%####((///((#&&&&&
*&&&&&/##%%%%%&&&&&&((#&&@@%%%%&&&@@@@&%%#((////////(//#&&&&&&%%####%%%%%%%%%&&%%%%%%%%%%%%%##%%%%%####(((((((%&&&&&
*&&&&&(##%%%&&&&&&&(#%&&&&%%%&@&&&&&&@@&%##((/(((//((//%%&&&&&%%###%%%%%%%%%%&&%%%%%%%%%%%%%#%%%%%%%###((((((#&&&&&&
*&&&&&(#%%%%&&&&&&%((%&&@@&%%%&@&%&&&&&&&&&&%%####(/(((/(#(#%&%%%%##%%%%%%%%%%&&%%%%%%%%%%%%%%%%%%%%%%####(((((%&&&&&&
*&&&&&(#%%%%&&&&((%&&&&&%#%@@&%%&@%%&@&&&@&%%###((#(//%&%#(/((####%%%%%%%%%%&&&%%%%%%%%&%%%%%%%%%%%%%###(((#%&&&&&&&
*&&&&&(#%%%%&&&%#(#&&&&&#%&@&%%&@%&@%%%&&&&&%%%%#((#&@@@((####%%%%%%%%%&&&&&%%%%%%&&%%%%%%%&&%%%%%###%&&&&&&&&&&
*&&&&&&%(##%%&&&%((%&&&&&#%@@&%%&@#%&#(&&&&@&&&&&&&&&@@@@@@&%%%%%%%%%&%%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&((#%%%&%(#%&&&&&%((%&&&%#%&&%##&&&%%(%&&&&&&&&&&&&&&&&&@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@&&&&&&%%%&&&&&&&
*&&&&&&(#%%%&&%&&&&&&(%&&&#&&&%##&&&&(%&&&&&&&&&&&%%&&&&&&&&@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@&&&&&&&&%%%&&&&&&&
*&&&&&&&%(##%%&&&&&&&&(%&&&&%(%&&&%&&&&&&((%&&&&&&&&%%%%%%%&&&&&&&&&&&@&&&&&&@@&&@@@@@@@@@@@@@&@@@&&&&&&&&&%%&&&&&&&
*&&&&&&&(#%%%&&&&&&&%(%&&&&#&&&&&(%&&&&&&%((#&&&&&&&%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%&&&&&&&
*&&&&&&&&(#%%&&&&&&#&&&&&%(%&&&&&(%&&&&&&&&%#%&&&&&&%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%%%%%%%&&&&
*&&&&&&&&&&%##%%&&&&(&&&&&&%(&&&&&#&&@@&&&&&&&&&&&&&&%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%%%%%%%%%%%%
*&&&&&&&&&&&%##%&&&&%#%@&&&&(&&&&&#&@@@@@&&&&&&&&&&&&&%%%%%%%%%&&&&&&&&&@@@@@@@@@@@@@&&&&&&&&&&&&&&&&&&&&&%%%&&&&&&&
*&&&&&&&&&&&&%%#%%%%#%&@&@&&&(#&&&&&@%(%&&@@@@@@@@@@@@&&&&&&%%%%%%&&&&&&&@@@@@@@@@@@@@@@@&@@@@@@&&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&%%%%&&&&&@&&%(%@@@@@&%@@@@@@@@@@@@@@@@&&&&%%%%%%&&&&&&&&@&&@&&&@@@@@@@@&@@@@@@@&&&&&&&&&&&&&&&&&&&&&&&
*&&&&&&&&&&&&&&&&&&&&&&&&&&&%#&&&&&&@@@@&&&&&&&&&&&&&@@@&&&&&%%%%%%&&&&&&&&&&@@@@@@@@&@@@&&@@&&&&&&&&&&&&&&&&&&&&&&&&&&&
  **/
case class DodgyPlan(steps: Seq[DodgyOp]) {
  override def toString: String = steps.collect {
    case Statement(op) =>
      op.format
    case Nop(message) =>
      message
    case v =>
      v.toString
  }.mkString("\n")
}