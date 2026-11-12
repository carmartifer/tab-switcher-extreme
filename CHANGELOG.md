<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Tab Switcher Extreme Changelog

## [Unreleased]

## [1.2.0]
### Changed
- Compatibility with IntelliJ IDEA 2025.2 through 2026.2 and newer (since build 252)
- Settings page moved to Settings | Tools | Tab Switcher Extreme
- Files are opened through the public FileEditorManager API instead of internal APIs

### Fixed
- The Apply button in the settings page is only enabled when something has changed
- Actions update on a background thread, as the current platform threading rules require

## [1.1.0]
### Fixed
- Updated to compitibility with 2021.3.X

## [1.0.5]
### Fixed
- Fix jumping to editor that was dragged outside of main window

## [1.0.4]
### Added
- Allow cycling through history with repeated keypressed. i.e. alt+a>a>a (or ctrl+tab>tab>tab). Without shift it cycles to older files, with shift to newer files.

## [1.0.3]
### Added
- Allow filenames in lists to remove certain parts (extension, prefix, any regex will do)

## [1.0.2]
### Added
- Remember desired position in lists (after up/down)

## [1.0.1]
### Fixed
- Fix crash

## [1.0.0]
### Added
- First release

