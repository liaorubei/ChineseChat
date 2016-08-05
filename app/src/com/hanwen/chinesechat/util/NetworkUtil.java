package com.hanwen.chinesechat.util;

import android.text.TextUtils;

public class NetworkUtil {
    //public static String domain = "http://192.168.3.121:801";
    //public static String domain = "http://voc2015test.azurewebsites.net";
    public static String domain = "http://voc2015.azurewebsites.net";
    public static final String chooseTeacher = domain + "/Api/nimUser/ChooseTeacherV1";//Id,target
    public static final String groupAccede = domain + "/Api/Group/Create";
    public static final String groupCreate = domain + "/Api/Group/Create";
    public static final String groupModify = domain + "/Api/Group/Create";
    public static final String groupRemove = domain + "/Api/Group/Remove";
    public static final String groupSelect = domain + "/Api/Group/Select";
    public static final String ObtainTeacher = domain + "/Api/nimUser/ObtainTeacher";
    public static final String studentCall = domain + "/Api/student/call";
    public static final String teacherEnqueue = domain + "/Api/NimUser/TeacherEnqueue";
    public static final String teacherDequeue = domain + "/Api/NimUser/TeacherDequeue";//Id
    public static final String teacherRefresh = domain + "/Api/NimUser/TeacherRefresh";
    public static final String teacherGroup = domain + "/Api/teacher/group";
    public static final String teacherInQueue = domain + "/Api/NimUser/TeacherInqueue";
    public static final String userCaptcha = domain + "/Api/NimUser/Captcha";
    public static final String userCreate = domain + "/Api/NimUser/create";
    public static final String userLogout = domain + "/Api/NimUser/logout";
    public static final String userSelect = domain + "/Api/NimUser/Select";//keyword,skip,take
    public static final String userSignIn = domain + "/Api/NimUser/Signin";//username,password,category,system,device
    public static final String userVerify = domain + "/Api/NimUser/Verify";
    public static final String ThemeSelect = domain + "/Api/Theme/Select";
    public static final String hsLevelAndTheme = domain + "/Api/Theme/HsLevelAndTheme";
    public static final String callCreate = domain + "/Api/CallLog/Start";
    public static final String callFinish = domain + "/Api/CallLog/finish";
    public static final String userGetByAccId = domain + "/Api/NimUser/GetByAccId";
    public static final String GetStudentCalllogByAccId = domain + "/Api/CallLog/GetStudentCalllogByAccId";
    public static final String themeGetById = domain + "/Api/Theme/GetById";
    public static final String calllogRating = domain + "/Api/CallLog/Rating";
    public static final String levelSelect = domain + "/Api/Level/Select";
    public static final String folderGetByLevelId = domain + "/Api/Folder/GetByLevelId";
    public static final String checkUpdate = domain + "/newclass/AndroidCheckUpdate";
    public static final String userUpdate = domain + "/Api/NimUser/Update";
    public static final String paymentCreateOrder = domain + "/Api/Payment/CreateOrder";
    public static final String paymentVerifyPayPal = domain + "/Api/Payment/VerifyPayPal";
    public static final String paymentVerifyAliPay = domain + "/Api/Payment/VerifyAliPay";
    public static final String paymentOrderRecords = domain + "/Api/Payment/OrderRecords";
    public static final String levelAndFolders = domain + "/Api/Level/SelectLevelAndFolders";
    public static String feedbackCreate = domain + "/Api/Feedback/Create";
    public static String productSelect = domain + "/Api/Product/Select";
    public static String nimuserGetCode = domain + "/Api/NimUser/GetCode";
    public static String nimuserVerify = domain + "/Api/NimUser/Verify";
    public static String nimUserChangePassword = domain + "/Api/NimUser/ChangePassword";
    public static String nimuserGetByUsername = domain + "/Api/NimUser/GetByUsername";
    public static String callRefresh = domain + "/Api/CallLog/Refresh";
    public static String GetStudentCallLogByUsername = domain + "/Api/CallLog/GetStudentByUsername";
    public static String GetTeacherCallLogByUsername = domain + "/Api/CallLog/GetTeacherByUsername";

    public static String chatAddTheme = domain + "/Api/CallLog/AddTheme";
    public static String getTeacher = domain + "/Api/NimUser/GetTeacher";
    public static String nimuserGetPhotosByUsername = domain + "/Api/NimUser/GetPhotosByUsername";
    public static String nimUserModifyPassword = domain + "/Api/NimUser/ModifyPassword";
    public static String nimUserUpdateTeacher = domain + "/Api/NimUser/UpdateTeacher";
    public static String nimUserUpdateStudent = domain + "/Api/NimUser/UpdateStudent";
    public static String CallLogGetByUsername = domain + "/Api/CallLog/GetByUsername";
    public static String CallLogGetByUserId = domain + "/Api/CallLog/GetByUserId";//id,skip,take,from,to
    public static String getTeacherOnline = domain + "/Api/NimUser/GetTeacherOnline";//skip,take
    public static String nimUserGetUserChatDataByAccid = domain + "/Api/NimUser/GetUserChatDataByAccid";//accid

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
