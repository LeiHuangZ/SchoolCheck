package com.example.huang.myapplication.retrofit;

import java.util.List;

/**
 * @author huang 3004240957@qq.com
 */
public class ContactBean {

    /**
     * serverResult : {"resultCode":200,"resultMessage":"操作成功"}
     * pageInfo : {"pageNum":1,"pageSize":20,"size":20,"startRow":null,"endRow":null,"total":"27","pages":2,"firstPage":1,"prePage":1,"nextPage":2,"lastPage":2,"isFirstPage":true,"isLastPage":false,"hasPreviousPage":false,"hasNextPage":true,"navigatePages":5,"navigatepageNums":[1,2],
     *          "list":[{"createTime":"1498534697","lastModifyTime":"2017-06-27 11:38:17","loginName":"18069853690","name":"张洪军","schName":"杭州校区","phone":"18069853690"},
     *          {"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853691","name":"张中西","schName":"杭州校区","phone":"18069853691"},
     *          {"createTime":"1495519076","lastModifyTime":"2017-05-23 13:57:56","loginName":"13157173412","name":"艾丽华","schName":"杭州校区","phone":"13157173412"},
     *          {"createTime":"1495696387","lastModifyTime":"2017-05-25 15:13:07","loginName":"13720634981","name":"李老师","schName":"杭州校区","phone":"13720634981"},{
     *          "createTime":"1495763263","lastModifyTime":"2017-05-26 09:47:43","loginName":"18858536912","name":"裘老师","schName":"杭州校区","phone":"18858536912"},
     *          {"createTime":"1496564380","lastModifyTime":"2017-06-04 16:19:40","loginName":"13575715140","name":"韩超","schName":"杭州校区","phone":"13575715140"},
     *          {"createTime":"1496564897","lastModifyTime":"2017-06-04 16:28:17","loginName":"13958162627","name":"陈顺永","schName":"杭州校区","phone":"13958162627"},
     *          {"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18989465418","name":"曹晓宏","schName":"杭州校区","phone":"18989465418"},{
     *          "createTime":"1495768235","lastModifyTime":"2017-05-26 11:10:35","loginName":"15355819191","name":"陈沁杰","schName":"杭州校区","phone":"15355819191"},
     *          {"createTime":"1497947893","lastModifyTime":"2017-06-20 16:38:13","loginName":"13067798558","name":"张铭鑫","schName":"杭州校区","phone":"13067798558"},
     *          {"createTime":"1516087887","lastModifyTime":"2018-01-16 15:31:27","loginName":"15281096276","name":"王老师","schName":"杭州校区","phone":"15281096276"},{
     *          "createTime":"1496383051","lastModifyTime":"2017-06-02 13:57:31","loginName":"13732288366","name":"马老师","schName":"杭州校区","phone":"13732288366"},
     *          {"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853692","name":"朱林","schName":"杭州校区","phone":"18069853692"},
     *          {"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853693","name":"赵雄","schName":"杭州校区","phone":"18069853693"},
     *          {"createTime":"1498632747","lastModifyTime":"2017-06-28 14:52:27","loginName":"15957178817","name":"高高","schName":"杭州校区","phone":"15957178817"},
     *          {"createTime":"1501750415","lastModifyTime":"2017-08-03 16:53:35","loginName":"18768184353","name":"田老师","schName":"杭州校区","phone":"18768184353"},
     *          {"createTime":"1501756449","lastModifyTime":"2017-08-03 18:34:09","loginName":"15091048144","name":"刘循","schName":"杭州校区","phone":"15091048144"},
     *          {"createTime":"1503478977","lastModifyTime":"2017-08-23 17:02:57","loginName":"15913190605","name":"冯伟","schName":"杭州校区","phone":"15913190605"},
     *          {"createTime":"1510811622","lastModifyTime":"2017-11-16 13:53:42","loginName":"18072982900","name":"吴迪","schName":"杭州校区","phone":"18072982900"},
     *          {"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18989472480","name":"马燚","schName":"杭州校区","phone":"18989472480"}]}
     */

    private ServerResultBean serverResult;
    private PageInfoBean pageInfo;

    public ServerResultBean getServerResult() {
        return serverResult;
    }

    public void setServerResult(ServerResultBean serverResult) {
        this.serverResult = serverResult;
    }

    public PageInfoBean getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfoBean pageInfo) {
        this.pageInfo = pageInfo;
    }

    public static class ServerResultBean {
        /**
         * resultCode : 200
         * resultMessage : 操作成功
         */

        private int resultCode;
        private String resultMessage;

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
    }

    public static class PageInfoBean {
        /**
         * pageNum : 1
         * pageSize : 20
         * size : 20
         * startRow : null
         * endRow : null
         * total : 27
         * pages : 2
         * firstPage : 1
         * prePage : 1
         * nextPage : 2
         * lastPage : 2
         * isFirstPage : true
         * isLastPage : false
         * hasPreviousPage : false
         * hasNextPage : true
         * navigatePages : 5
         * navigatepageNums : [1,2]
         * list : [{"createTime":"1498534697","lastModifyTime":"2017-06-27 11:38:17","loginName":"18069853690","name":"张洪军","schName":"杭州校区","phone":"18069853690"},{"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853691","name":"张中西","schName":"杭州校区","phone":"18069853691"},{"createTime":"1495519076","lastModifyTime":"2017-05-23 13:57:56","loginName":"13157173412","name":"艾丽华","schName":"杭州校区","phone":"13157173412"},{"createTime":"1495696387","lastModifyTime":"2017-05-25 15:13:07","loginName":"13720634981","name":"李老师","schName":"杭州校区","phone":"13720634981"},{"createTime":"1495763263","lastModifyTime":"2017-05-26 09:47:43","loginName":"18858536912","name":"裘老师","schName":"杭州校区","phone":"18858536912"},{"createTime":"1496564380","lastModifyTime":"2017-06-04 16:19:40","loginName":"13575715140","name":"韩超","schName":"杭州校区","phone":"13575715140"},{"createTime":"1496564897","lastModifyTime":"2017-06-04 16:28:17","loginName":"13958162627","name":"陈顺永","schName":"杭州校区","phone":"13958162627"},{"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18989465418","name":"曹晓宏","schName":"杭州校区","phone":"18989465418"},{"createTime":"1495768235","lastModifyTime":"2017-05-26 11:10:35","loginName":"15355819191","name":"陈沁杰","schName":"杭州校区","phone":"15355819191"},{"createTime":"1497947893","lastModifyTime":"2017-06-20 16:38:13","loginName":"13067798558","name":"张铭鑫","schName":"杭州校区","phone":"13067798558"},{"createTime":"1516087887","lastModifyTime":"2018-01-16 15:31:27","loginName":"15281096276","name":"王老师","schName":"杭州校区","phone":"15281096276"},{"createTime":"1496383051","lastModifyTime":"2017-06-02 13:57:31","loginName":"13732288366","name":"马老师","schName":"杭州校区","phone":"13732288366"},{"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853692","name":"朱林","schName":"杭州校区","phone":"18069853692"},{"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18069853693","name":"赵雄","schName":"杭州校区","phone":"18069853693"},{"createTime":"1498632747","lastModifyTime":"2017-06-28 14:52:27","loginName":"15957178817","name":"高高","schName":"杭州校区","phone":"15957178817"},{"createTime":"1501750415","lastModifyTime":"2017-08-03 16:53:35","loginName":"18768184353","name":"田老师","schName":"杭州校区","phone":"18768184353"},{"createTime":"1501756449","lastModifyTime":"2017-08-03 18:34:09","loginName":"15091048144","name":"刘循","schName":"杭州校区","phone":"15091048144"},{"createTime":"1503478977","lastModifyTime":"2017-08-23 17:02:57","loginName":"15913190605","name":"冯伟","schName":"杭州校区","phone":"15913190605"},{"createTime":"1510811622","lastModifyTime":"2017-11-16 13:53:42","loginName":"18072982900","name":"吴迪","schName":"杭州校区","phone":"18072982900"},{"createTime":"0","lastModifyTime":"1970-01-01 08:00:00","loginName":"18989472480","name":"马燚","schName":"杭州校区","phone":"18989472480"}]
         */

        private int pageNum;
        private int pageSize;
        private int size;
        private Object startRow;
        private Object endRow;
        private String total;
        private int pages;
        private int firstPage;
        private int prePage;
        private int nextPage;
        private int lastPage;
        private boolean isFirstPage;
        private boolean isLastPage;
        private boolean hasPreviousPage;
        private boolean hasNextPage;
        private int navigatePages;
        private List<Integer> navigatepageNums;
        private List<ListBean> list;

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public Object getStartRow() {
            return startRow;
        }

        public void setStartRow(Object startRow) {
            this.startRow = startRow;
        }

        public Object getEndRow() {
            return endRow;
        }

        public void setEndRow(Object endRow) {
            this.endRow = endRow;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getFirstPage() {
            return firstPage;
        }

        public void setFirstPage(int firstPage) {
            this.firstPage = firstPage;
        }

        public int getPrePage() {
            return prePage;
        }

        public void setPrePage(int prePage) {
            this.prePage = prePage;
        }

        public int getNextPage() {
            return nextPage;
        }

        public void setNextPage(int nextPage) {
            this.nextPage = nextPage;
        }

        public int getLastPage() {
            return lastPage;
        }

        public void setLastPage(int lastPage) {
            this.lastPage = lastPage;
        }

        public boolean isIsFirstPage() {
            return isFirstPage;
        }

        public void setIsFirstPage(boolean isFirstPage) {
            this.isFirstPage = isFirstPage;
        }

        public boolean isIsLastPage() {
            return isLastPage;
        }

        public void setIsLastPage(boolean isLastPage) {
            this.isLastPage = isLastPage;
        }

        public boolean isHasPreviousPage() {
            return hasPreviousPage;
        }

        public void setHasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public int getNavigatePages() {
            return navigatePages;
        }

        public void setNavigatePages(int navigatePages) {
            this.navigatePages = navigatePages;
        }

        public List<Integer> getNavigatepageNums() {
            return navigatepageNums;
        }

        public void setNavigatepageNums(List<Integer> navigatepageNums) {
            this.navigatepageNums = navigatepageNums;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * createTime : 1498534697
             * lastModifyTime : 2017-06-27 11:38:17
             * loginName : 18069853690
             * name : 张洪军
             * schName : 杭州校区
             * phone : 18069853690
             */

            private String createTime;
            private String lastModifyTime;
            private String loginName;
            private String name;
            private String schName;
            private String phone;

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }

            public String getLastModifyTime() {
                return lastModifyTime;
            }

            public void setLastModifyTime(String lastModifyTime) {
                this.lastModifyTime = lastModifyTime;
            }

            public String getLoginName() {
                return loginName;
            }

            public void setLoginName(String loginName) {
                this.loginName = loginName;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSchName() {
                return schName;
            }

            public void setSchName(String schName) {
                this.schName = schName;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }
        }
    }
}
