{
  description = "Rebobina - Android TV handoff lab";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  };

  outputs =
    { nixpkgs, ... }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
      ];

      forAllSystems =
        f:
        nixpkgs.lib.genAttrs systems (
          system:
          f (
            import nixpkgs {
              inherit system;
              config.allowUnfree = true;
            }
          )
        );

      androidPackagesFor =
        pkgs:
        pkgs.androidenv.composeAndroidPackages {
          platformVersions = [ "36" ];
          buildToolsVersions = [ "35.0.0" ];
          abiVersions = [
            "arm64-v8a"
            "x86_64"
          ];
          includeEmulator = false;
          includeSystemImages = false;
        };
    in
    {
      devShells = forAllSystems (
        pkgs:
        let
          androidPackages = androidPackagesFor pkgs;
        in
        {
          default = pkgs.mkShell {
            packages = [
              pkgs.android-tools
              pkgs.difftastic
              pkgs.gradle
              pkgs.jdk17
              pkgs.jq
              pkgs.jujutsu
              pkgs.just
            ];

            JAVA_HOME = "${pkgs.jdk17}";
          };

          android = pkgs.mkShell {
            packages = [
              pkgs.android-tools
              pkgs.difftastic
              pkgs.gradle
              pkgs.jdk17
              pkgs.jq
              pkgs.jujutsu
              pkgs.just
              androidPackages.androidsdk
            ];

            ANDROID_HOME = "${androidPackages.androidsdk}/libexec/android-sdk";
            ANDROID_SDK_ROOT = "${androidPackages.androidsdk}/libexec/android-sdk";
            JAVA_HOME = "${pkgs.jdk17}";
          };
        }
      );
    };
}
