package com.libsdhqs.jni;

public abstract class QSLibSDH {

    public record ListItem(String image,  String text) { }
    public record ExecutionState(String loc, int actIndex, int lineNum) { }
    public record VarValResp(boolean isSuccess, String stringValue, int intValue) { }
    public record ErrorData(String locName, int errorNum, int index, int line) { }

    static {
        System.loadLibrary("qslibsdh");
    }

    // --- Lifecycle and State ---
    public native void init();
    public native void terminate();
    public native void enableDebugMode(boolean isDebug);
    public native ExecutionState getCurStateData();
    public native int getFullRefreshCount();
    public native String getQstFullPath();
    public native String getCurLoc();
    public native Object getExprValue();

    // --- Version and compile date ---
    public native String getVersion();
    public native String getCompiledDateTime();

    // --- Main Description ---
    public native String getMainDesc();
    public native boolean isMainDescChanged();

    // --- Variables Description ---
    public native String getVarsDesc();
    public native boolean isVarsDescChanged();
    public native int getVarValuesCount(String name);
    public native VarValResp getVarValues(String name, int ind);
    public native int getMaxVarsCount();
    public native Object getVarNameByIndex();

    // --- Input ---
    public native void setInputStrText(String text);

    // --- Actions ---
    public native ListItem[] getActions();
    public native boolean isActionsChanged();
    public native boolean executeSelActionCode(boolean isRefresh);
    public native int getSelActionIndex();
    public native boolean setSelActionIndex(int ind, boolean isRefresh);

    // --- Objects ---
    public native ListItem[] getObjects();
    public native boolean isObjectsChanged();
    public native int getSelObjectIndex();
    public native boolean setSelObjectIndex(int ind, boolean isRefresh);

    // --- Code execution ---
    public native boolean execString(String s, boolean isRefresh);
    public native boolean execLocationCode(String name, boolean isRefresh);
    public native boolean execCounter(boolean isRefresh);
    public native boolean execUserInput(boolean isRefresh);

    // --- Errors ---
    public native ErrorData getLastErrorData();
    public native String getErrorDesc(int errorNum);

    // --- Game ---
    public native boolean restartGame(boolean isRefresh);

    public native boolean loadGameWorldFromData(byte[] data, String fileName, boolean isRefresh);
    public native boolean loadGameWorldFromFD(int fileDescriptor, String fileName, boolean isRefresh);
    public native boolean loadGameWorldFromPath(String filePath, String fileName, boolean isRefresh);

    public native byte[] saveGameAsData(boolean isRefresh);
    public native boolean saveGameByFD(int fileDescriptor, boolean isRefresh);
    public native boolean saveGameByPath(String filePath, boolean isRefresh);

    public native boolean openSavedGameFromData(byte[] data, boolean isRefresh);
    public native boolean openSavedGameFromFD(int fileDescriptor, boolean isRefresh);
    public native boolean openSavedGameFromPath(String filePath, boolean isRefresh);

    // --- Menu ---
    public native void selectMenuItem(int index);

    // --- Callbacks from Native Code ---
    public void onCallDebug(String str) {}
    public void onRefreshInt() {}

    public void onShowImage(String path) {}
    public void onShowMessage(String message) {}
    public void onShowWindow(int type, boolean isShow) {}

    public void onPlayFile(String path, int volume) {}
    public boolean onIsPlayingFile(String path) { return false; }
    public void onCloseFile(String path) {}

    public void onOpenGame(String file, boolean isNewGame) {}
    public void onOpenGameStatus(String filename) {}
    public void onSaveGameStatus(String filename) {}

    public String onInputBox(String prompt) { return ""; }

    public void onSetTimer(int msecs) {}
    public int onGetMsCount() { return 0; }
    public void onSleep(int msecs) {}

    public void onAddMenuItem(String name, String imgPath) {}
    public void onShowMenuOld() {}
    public int onShowMenuNew() { return -1; }
    public void onDeleteMenu() {}

    public void onChangeQuestPath(String path) {}
}