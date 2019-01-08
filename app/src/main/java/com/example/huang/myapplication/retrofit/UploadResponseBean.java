package com.example.huang.myapplication.retrofit;

/**
 * @author huang 3004240957@qq.com
 */
public class UploadResponseBean {

    /**
     * serverResult : {"resultCode":200,"resultMessage":"操作成功","internalMessage":null}
     * pageInfo : null
     */

    private ServerResultBean serverResult;
    private Object pageInfo;

    public ServerResultBean getServerResult() {
        return serverResult;
    }

    public void setServerResult(ServerResultBean serverResult) {
        this.serverResult = serverResult;
    }

    public Object getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(Object pageInfo) {
        this.pageInfo = pageInfo;
    }

    public static class ServerResultBean {
        /**
         * resultCode : 200
         * resultMessage : 操作成功
         * internalMessage : null
         */

        private int resultCode;
        private String resultMessage;
        private String internalMessage;

        public int getResultCode() {
            return resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMessage() {
            return resultMessage;
        }

        public void setResultMessage(String resultMessage) {
            this.resultMessage = resultMessage;
        }

        public String getInternalMessage() {
            return internalMessage;
        }

        public void setInternalMessage(String internalMessage) {
            this.internalMessage = internalMessage;
        }
    }
}
