# Web Art Visual Tracker

A Java-based tool for analyzing dynamic visual changes in web-based artworks.

## Features

- Tracks and saves screenshots of visual changes
- Detects mouse and keyboard input (movement, clicks, scrolls, typing)
- Logs events with timestamps to a CSV file
- Organizes each session in its own folder
- Simple, extensible structure for further analysis or visualization

## How to use

1. Clone the repository
2. Make sure you have Java and Chrome installed
3. Set the path to your ChromeDriver in `VisualTracker.java`
4. Run the program using your IDE or terminal
5. Press [Enter] to stop tracking

## Folder structure
/screenshots/
  /session_YYYYMMDD_HHMMSS/
    screenshot_*.png
    event_log.csv
/src/
README.md

