; NetHelt desktop client installer (Inno Setup 6).
;
; Built by the distribution module on Windows (mvn package -Pdist), which passes:
;   /DAppVersion=1.2.3 /DAppImageDir=<app-image\nethelt> /DWinSWExe=<WinSW-x64.exe> /DOutputDir=<target>
; Defaults below allow compiling it by hand after a dist build.
;
; Silent install:   nethelt-setup-<version>-windows-x64.exe /VERYSILENT /SUPPRESSMSGBOXES
; Silent uninstall: unins000.exe /VERYSILENT [/PURGE]   (/PURGE also removes credentials and logs)

#ifndef AppVersion
  #define AppVersion "1.0.0"
#endif
#ifndef AppImageDir
  #define AppImageDir "..\..\target\app-image\nethelt"
#endif
#ifndef WinSWExe
  #define WinSWExe "..\..\target\winsw\WinSW-x64.exe"
#endif
#ifndef OutputDir
  #define OutputDir "..\..\target"
#endif

[Setup]
; Never change AppId, upgrades are matched by it
AppId={{1E35E58D-0E1F-47A4-82C0-FA9B776C8AF9}
AppName=NetHelt
AppVersion={#AppVersion}
AppVerName=NetHelt {#AppVersion}
AppPublisher=Sebastian Górski
AppPublisherURL=https://github.com/sgorski00/nethelt
VersionInfoVersion={#AppVersion}
DefaultDirName={autopf}\NetHelt
DisableProgramGroupPage=yes
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
ChangesEnvironment=yes
UninstallDisplayIcon={app}\nethelt.exe
OutputDir={#OutputDir}
OutputBaseFilename=nethelt-setup-{#AppVersion}-windows-x64
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
SetupIconFile=..\icons\nethelt.ico
WizardSmallImageFile=..\icons\wizard-small-55.bmp,..\icons\wizard-small-110.bmp
SetupLogging=yes

[Types]
Name: "full"; Description: "Full installation"
Name: "custom"; Description: "Custom installation"; Flags: iscustom

[Components]
Name: "core"; Description: "NetHelt CLI and background service (required)"; Types: full custom; Flags: fixed
; Optional components. Files of a component are named after its launcher (see src/assembly/core.xml), e.g.:
; Name: "gui"; Description: "Desktop GUI application"; Types: full

[Files]
Source: "{#AppImageDir}\*"; DestDir: "{app}"; Excludes: "nethelt-gui*"; Components: core; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#WinSWExe}"; DestDir: "{app}"; DestName: "nethelt-agent-service.exe"; Components: core; Flags: ignoreversion
Source: "nethelt-agent-service.xml"; DestDir: "{app}"; Components: core; Flags: ignoreversion
; Source: "{#AppImageDir}\nethelt-gui*"; DestDir: "{app}"; Components: gui; Flags: ignoreversion
; Source: "{#AppImageDir}\app\nethelt-gui*"; DestDir: "{app}\app"; Components: gui; Flags: ignoreversion

[UninstallDelete]
Type: dirifempty; Name: "{app}"

[Code]
const
  ServiceId = 'nethelt-agent';
  EnvironmentKey = 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment';
  // Well-known SIDs, the account names are localized: SYSTEM and BUILTIN\Administrators
  ConfigDirAcl = '/inheritance:r /grant:r *S-1-5-18:(OI)(CI)F *S-1-5-32-544:(OI)(CI)F';

var
  PatPage: TInputQueryWizardPage;

function ConfigDir: String;
begin
  Result := ExpandConstant('{commonappdata}\NetHelt');
end;

function CredentialsFile: String;
begin
  Result := ConfigDir + '\credentials';
end;

function ExecHidden(const FileName, Params: String): Integer;
var
  ResultCode: Integer;
begin
  if Exec(FileName, Params, '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
    Result := ResultCode
  else
    Result := -1;
end;

function SystemTool(const Name: String): String;
begin
  Result := ExpandConstant('{sys}\' + Name);
end;

{ Service }

function ServiceExists: Boolean;
begin
  Result := ExecHidden(SystemTool('sc.exe'), 'query ' + ServiceId) = 0;
end;

// Stops and deletes the service, so files can be replaced and the new WinSW config is applied on install
procedure RemoveService;
var
  Attempt: Integer;
begin
  if not ServiceExists then
    Exit;
  // net stop waits until the service is stopped, sc stop does not
  ExecHidden(SystemTool('net.exe'), 'stop ' + ServiceId);
  ExecHidden(SystemTool('sc.exe'), 'delete ' + ServiceId);
  // The service stays "marked for deletion" while something holds a handle to it
  Attempt := 0;
  while ServiceExists and (Attempt < 20) do
  begin
    Sleep(500);
    Attempt := Attempt + 1;
  end;
end;

procedure InstallService;
var
  Wrapper: String;
begin
  Wrapper := ExpandConstant('{app}\nethelt-agent-service.exe');
  if ExecHidden(Wrapper, 'install') <> 0 then
  begin
    SuppressibleMsgBox('Registering the NetHelt Agent service failed. Run "' + Wrapper + ' install" as administrator.',
      mbError, MB_OK, IDOK);
    Exit;
  end;
  if ExecHidden(Wrapper, 'start') <> 0 then
    SuppressibleMsgBox('Starting the NetHelt Agent service failed. Check the logs in ' + ConfigDir + '\logs.',
      mbError, MB_OK, IDOK);
end;

{ Files and PATH }

// Credentials and logs, readable only by SYSTEM (the service account) and administrators
procedure RestrictConfigDir;
begin
  ForceDirectories(ConfigDir);
  if ExecHidden(SystemTool('icacls.exe'), '"' + ConfigDir + '" ' + ConfigDirAcl) <> 0 then
    Log('Failed to restrict access to ' + ConfigDir);
end;

function PathContains(const Paths, Dir: String): Boolean;
begin
  Result := Pos(';' + Uppercase(Dir) + ';', ';' + Uppercase(Paths) + ';') > 0;
end;

procedure AddToPath(const Dir: String);
var
  Paths: String;
begin
  if not RegQueryStringValue(HKLM, EnvironmentKey, 'Path', Paths) then
    Paths := '';
  if PathContains(Paths, Dir) then
    Exit;
  if (Paths <> '') and (Copy(Paths, Length(Paths), 1) <> ';') then
    Paths := Paths + ';';
  RegWriteExpandStringValue(HKLM, EnvironmentKey, 'Path', Paths + Dir);
end;

procedure RemoveFromPath(const Dir: String);
var
  Paths: String;
  Position: Integer;
begin
  if not RegQueryStringValue(HKLM, EnvironmentKey, 'Path', Paths) then
    Exit;
  Paths := ';' + Paths + ';';
  Position := Pos(';' + Uppercase(Dir) + ';', Uppercase(Paths));
  if Position = 0 then
    Exit;
  Delete(Paths, Position, Length(Dir) + 1);
  RegWriteExpandStringValue(HKLM, EnvironmentKey, 'Path', Copy(Paths, 2, Length(Paths) - 2));
end;

{ PAT }

procedure InitializeWizard;
begin
  PatPage := CreateInputQueryPage(wpSelectComponents,
    'Agent registration', 'Register this computer as a NetHelt agent.',
    'Paste the Personal Access Token generated in the NetHelt web app. ' +
    'Leave it empty to register later with "nethelt auth register" in a terminal run as administrator.');
  PatPage.Add('Personal Access Token:', True);
end;

// Upgrades keep the registered PAT
function ShouldSkipPage(PageID: Integer): Boolean;
begin
  Result := (PageID = PatPage.ID) and FileExists(CredentialsFile);
end;

// The CLI reads the PAT from stdin, so it never shows up in a command line
procedure RegisterPat;
var
  Pat, PatFile, Cli: String;
begin
  Pat := Trim(PatPage.Values[0]);
  if Pat = '' then
    Exit;
  PatFile := ExpandConstant('{tmp}\pat.txt');
  Cli := ExpandConstant('{app}\nethelt.exe');
  if SaveStringToFile(PatFile, Pat + #13#10, False) and
     (ExecHidden(ExpandConstant('{cmd}'), '/c ""' + Cli + '" auth register --stdin < "' + PatFile + '""') = 0) then
    Log('Personal Access Token registered')
  else
    SuppressibleMsgBox('Registering the Personal Access Token failed. ' +
      'Run "nethelt auth register" in a terminal run as administrator.', mbError, MB_OK, IDOK);
  DeleteFile(PatFile);
end;

{ Install / uninstall steps }

function PrepareToInstall(var NeedsRestart: Boolean): String;
begin
  RemoveService;
  Result := '';
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    RestrictConfigDir;
    AddToPath(ExpandConstant('{app}'));
    RegisterPat;
    InstallService;
  end;
end;

function PurgeRequested: Boolean;
begin
  if Pos('/PURGE', Uppercase(GetCmdTail)) > 0 then
    Result := True
  else if UninstallSilent then
    Result := False
  else
    Result := MsgBox('Also remove the registered Personal Access Token and logs (' + ConfigDir + ')?',
      mbConfirmation, MB_YESNO or MB_DEFBUTTON2) = IDYES;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  case CurUninstallStep of
    usUninstall:
      begin
        RemoveService;
        RemoveFromPath(ExpandConstant('{app}'));
      end;
    usPostUninstall:
      if PurgeRequested then
        DelTree(ConfigDir, True, True, True);
  end;
end;
