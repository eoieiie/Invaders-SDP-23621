# Hello World

## Team Introduction

Hello World is responsible for the Main Menu.

Our goal is to develop the user interface for the game's main menu and other screens
so that the features implemented by other teams operate without any issues.

## Members

| Name | Role | GitHub |
| --- | --- | --- |
| Byeongjoo Hwang | Team leader, cross-team coordination, CI | [eoieiie](https://github.com/eoieiie) |
| Junwoo Kang | Shop screen shell | [oofrog](https://github.com/oofrog) |
| Yongtae Kim | Settings UI, main repository access | [kyle891204](https://github.com/kyle891204) |
| Jaeone Park | Key hints, exit confirmation | [kyobak](https://github.com/kyobak) |
| Taehyun Bak | Ship select screen shell | [nuyh-99](https://github.com/nuyh-99) |
| Myeongho Song | Mouse support | [SongMyeongHo](https://github.com/SongMyeongHo) |
| Changyong Woo | Documentation, wiki | [samryong](https://github.com/samryong) |
| Hyeokjun Lee | Menu framework | [Aninnom](https://github.com/Aninnom) |
| Junhyeok Han | Achievements screen shell | [junh000](https://github.com/junh000) |

## Team Requirements

- Improve Main Menu usability
- Develop Settings UI
- Build the Achievements screen shell (contents by the Records & Achievements team)
- Build the Shop screen shell (contents by the Currency and Item teams)

## Detailed Requirements

* **Menu framework:** Replace the hard-coded options in `TitleScreen` with a menu item list so that other teams can add a screen by adding one item.
* **Mouse support:** Menu items can be selected by mouse click, with hover highlight.
* **Key hints & exit confirmation:** Footer showing available keys; Exit asks for confirmation.
* **Settings UI:** Volume controls (BGM/SFX) and key binding display.
* **Shop screen shell:** Screen reachable from the menu with a placeholder layout. Currency, items, and purchase logic are provided by the Currency and Item teams.
* **Achievements screen shell:** Screen reachable from the menu with a placeholder layout. Achievement data and unlock logic are provided by the Records & Achievements team.

## Dependencies on Other Teams

* **Shop screen:** Requires `Currency System` & `Item System`
* **Settings UI:** Requires `Sound Effects / BGM`
* **Achievements screen:** Requires `Records & Achievements System`
