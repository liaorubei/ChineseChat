package com.hanwen.chinesechat.util;

import android.text.TextUtils;

public class NetworkUtil {
    //public static String domain = "http://192.168.3.121:801";
    //public static String domain = "http://voc2015test.azurewebsites.net";
    public static String domain = "http://voc2015.azurewebsites.net";

    public static final String callCreate = domain + "/Api/CallLog/Start";
    public static final String callFinish = domain + "/Api/CallLog/finish";
    public static final String CallLogGetByUsername = domain + "/Api/CallLog/GetByUsername";
    public static final String CallLogGetListByUserId = domain + "/Api/CallLog/GetListByUserId";//id,skip,take,from,to
    public static final String calllogRating = domain + "/Api/CallLog/Rating";
    public static final String callRefresh = domain + "/Api/CallLog/Refresh";
    public static final String chatAddTheme = domain + "/Api/CallLog/AddTheme";
    public static final String checkUpdate = domain + "/NewClass/AndroidCheckUpdate";
    public static final String chooseTeacher = domain + "/Api/nimUser/ChooseTeacherV1";//Id,target
    public static final String documentGetById = domain + "/Api/Document/GetById";//id
    public static final String documentGetListByLevelId = domain + "/Api/Document/GetListByLevelId";//levelId,skip,take
    public static final String documentGetListByFolderId = domain + "/Api/Document/GetListByFolderId";//folderId,userId,skip,take
    public static final String documentGetListByFolderIdWithoutCheck = domain + "/Api/Document/GetListByFolderIdWithoutCheck";//folderId,skip,take
    public static final String feedbackCreate = domain + "/Api/Feedback/Create";
    public static final String folderCheckPermission = domain + "/Api/Folder/CheckPermission";//folderId,userId
    public static final String folderGetChildListByParentId = domain + "/Api/Folder/GetChildListByParentId";//folderId
    public static final String folderGetListByLevelId = domain + "/Api/Folder/GetListByLevelIdV2";//levelId,skip,take
    public static final String getTeacherOnline = domain + "/Api/NimUser/GetTeacherOnline";//skip,take
    public static final String hsLevelAndTheme = domain + "/Api/Theme/HsLevelAndTheme";
    public static final String levelAndFolders = domain + "/Api/Level/SelectLevelAndFolders";
    public static final String levelSelect = domain + "/Api/Level/Select";
    public static final String nimUserChangePassword = domain + "/Api/NimUser/ChangePassword";
    public static final String nimuserGetByUsername = domain + "/Api/NimUser/GetByUsername";
    public static final String nimuserGetCode = domain + "/Api/NimUser/GetCode";
    public static final String nimuserGetPhotosByUsername = domain + "/Api/NimUser/GetPhotosByUsername";
    public static final String nimUserGetUserChatDataByAccid = domain + "/Api/NimUser/GetUserChatDataByAccid";//accid
    public static final String nimUserModifyPassword = domain + "/Api/NimUser/ModifyPassword";
    public static final String nimUserUpdateStudent = domain + "/Api/NimUser/UpdateStudent";
    public static final String nimUserUpdateTeacher = domain + "/Api/NimUser/UpdateTeacher";
    public static final String nimuserVerify = domain + "/Api/NimUser/Verify";
    public static final String paymentCreateOrder = domain + "/Api/Payment/CreateOrder";
    public static final String paymentOrderRecords = domain + "/Api/Payment/OrderRecords";
    public static final String paymentVerifyAliPay = domain + "/Api/Payment/VerifyAliPay";
    public static final String paymentVerifyPayPal = domain + "/Api/Payment/VerifyPayPal";
    public static final String productSelect = domain + "/Api/Product/Select";
    public static final String studentCall = domain + "/Api/student/call";
    public static final String teacherDequeue = domain + "/Api/NimUser/TeacherDequeue";//Id
    public static final String teacherEnqueue = domain + "/Api/NimUser/TeacherEnqueue";
    public static final String teacherRefresh = domain + "/Api/NimUser/TeacherRefresh";//id,isOnline
    public static final String themeGetById = domain + "/Api/Theme/GetById";
    public static final String ThemeSelect = domain + "/Api/Theme/Select";
    public static final String userCreate = domain + "/Api/NimUser/create";
    public static final String userGetByAccId = domain + "/Api/NimUser/GetByAccId";
    public static final String userLogout = domain + "/Api/NimUser/logout";
    public static final String userSelect = domain + "/Api/NimUser/Select";//keyword,skip,take
    public static final String userSignIn = domain + "/Api/NimUser/Signin";//username,password,category,system,device
    public static final String userUpdate = domain + "/Api/NimUser/Update";
    public static final String userVerify = domain + "/Api/NimUser/Verify";
    public static String hskkGetListByRankAndPart = domain + "/Api/Hskk/GetListByRankAndPart";//rank,part,skip,take
    public static String hskkGetById = domain + "/Api/Hskk/GetById";//id

    public static String format(String text, Object... para) {
        for (int i = 0; i < para.length; i++) {
            text = text.replaceAll("\\{" + i + "\\}", para[i] + "");
        }
        return text;
    }

    public static String getDocById(int id) {

        return domain + "/NewClass/DocById/" + id;
    }

    public static String getDocs(String folderId, String skip, String take) {
        return domain + format("/NewClass/GetDocs?folderId={0}&skip={1}&take={2}", folderId, skip, take);// "/NewClass/GetDocs?folderId=" + folderId + "&skip=" + skip + "&take=" + take;
    }

    /**
     * 把一个网站的相对路径转为这个网站的标准HTTP全路径
     *
     * @param path
     * @return
     */
    public static String getFullPath(String path) {
        return TextUtils.isEmpty(path) ? null : domain + path;
    }
}
