# 4.4.7-fly

- Fixed a Fabric startup crash with Farmer's Delight 3.6.11 by creating the fog particle type through Fabric API instead of Minecraft's protected constructor ([#1](https://github.com/bobqianic/BrewinAndChewin-Fly/issues/1)).
- Fixed a Fabric crash when opening a Keg containing water, caused by synchronized recipes being accessed through the wrong Fabric API interface ([#2](https://github.com/bobqianic/BrewinAndChewin-Fly/issues/2)).
- Thanks to [AlaxeiStolas](https://github.com/AlaxeiStolas) and [LudwigZwei](https://github.com/LudwigZwei) for reporting these issues.
