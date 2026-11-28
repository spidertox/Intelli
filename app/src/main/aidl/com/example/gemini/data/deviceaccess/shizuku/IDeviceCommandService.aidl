package com.example.gemini.data.deviceaccess.shizuku;

/** Execute one argument-vector process as Shizuku's authorized shell/root UID. */
interface IDeviceCommandService {
    String execute(String requestJson);
    void cancel(String requestId);
    void destroy();
}
