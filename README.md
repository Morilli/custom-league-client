# custom-league-client

You can support me and this project by buying a coffee

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/hawolt)

## Bugs & Feature Requests

if you are experiences any troubles or would like a feature please open a new Issue and chose the correct template.

you can click [here](https://github.com/hawolt/custom-league-client/issues/new/choose) to open a new issue

## Dependencies

- [riot-xmpp](https://github.com/hawolt/riot-xmpp)
- [league-of-legends-rtmp](https://github.com/hawolt/league-of-legends-rtmp)
- [league-client-api](https://github.com/hawolt/league-client-api)
- [league-of-legends-rms](https://github.com/hawolt/league-of-legends-rms)

## Discord

since this code lacks documentation the best help you can get is my knowledge, proper questions can be asked in
this [discord](https://discord.gg/3wknX5gxaW) server, please note that I will not guide you to achieve something or
answer beginner level questions

## How to setup the project using IntelliJ

1. within Intellij select `File` -> `New` -> `Project from Version Control...`
2. insert `git@github.com:hawolt/custom-league-client.git` for the `URL` field and hit `Clone`
3. IntelliJ should automatically detect the Maven framework, if this is not the case you can rightclick the
   custom-league-client folder in the Project hierarchy and select `Add Framework Support...` then select `Maven`
4. Make sure you are actually using a compatible Java version by selecting `File` -> `Project Structure`, navigate
   to `Project` within `Project Settings` and make sure both `SDK` and `Language level` have Java Version 17 or higher
   selected, hit `OK`
5. To run the Code navigate to `SwingUI/src/main/java/com/hawolt` and rightclick `LeagueClientUI`,
   select `Run LeagueClientUI.main()`

## Contributions

Pull requests are always appreciated, please note that static data sources will not get merged as the data is available
in the local game files, while they might not be present currently they will be at later stages of development, before
writing a larger chunk of code please communicate on Discord if it is needed
