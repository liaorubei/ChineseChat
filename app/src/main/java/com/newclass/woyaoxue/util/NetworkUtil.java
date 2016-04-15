package com.newclass.woyaoxue.util;

public class NetworkUtil {
    // public static String domain = "http://192.168.3.121:801";
    public static String domain = "http://voc2015.azurewebsites.net";
    public static final String chooseTeacher = domain + "/Api/nimUser/ChooseTeacher";
    public static final String groupAccede = domain + "/Api/Group/Create";
    public static final String groupCreate = domain + "/Api/Group/Create";
    public static final String groupModify = domain + "/Api/Group/Create";
    public static final String groupRemove = domain + "/Api/Group/Remove";
    public static final String groupSelect = domain + "/Api/Group/Select";
    public static final String ObtainTeacher = domain + "/Api/nimUser/ObtainTeacher";
    public static final String studentCall = domain + "/Api/student/call";
    public static final String teacherEnqueue = domain + "/Api/NimUser/TeacherEnqueue";
    public static final String teacherRefresh = domain + "/Api/NimUser/TeacherRefresh";
    public static final String teacherGroup = domain + "/Api/teacher/group";
    public static final String teacherInQueue = domain + "/Api/NimUser/TeacherInqueue";
    public static final String userCaptcha = domain + "/Api/NimUser/Captcha";
    public static final String userCreate = domain + "/Api/NimUser/create";
    public static final String userLogout = domain + "/Api/NimUser/logout";
    public static final String userSelect = domain + "/Api/NimUser/Select";
    public static final String userSignIn = domain + "/Api/NimUser/Signin";
    public static final String userVerify = domain + "/Api/NimUser/Verify";
    public static final String ThemeSelect = domain + "/Api/Theme/Select";
    public static final String hsLevelAndTheme = domain + "/Api/Theme/HsLevelAndTheme";
    public static final String callstart = domain + "/Api/CallLog/Start";
    public static final String callFinish = domain + "/Api/Calllog/finish";
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
    public static String nimuserChangePassword = domain + "/Api/NimUser/ChangePassword";
    public static String nimuserGetByUsername = domain + "/Api/NimUser/GetByUsername";
    public static String callRefresh = domain + "/Api/CallLog/Refresh";
    public static String GetStudentCallLogByUsername = domain + "/Api/CallLog/GetStudentByUsername";
    public static String chatAddTheme = domain + "/Api/CallLog/AddTheme";
    public static String getTeacher = domain + "/Api/NimUser/GetTeacher";

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
     * /NewClass/DocsByLevelId/{levelId}
     *
     * @param id
     * @return
     */
    public static String getDocsByLevelId(int id) {
        return domain + "/NewClass/DocsByLevelId/" + id;
    }

    /**
     * @param levelId 取得指定LevelId下文件夹内文档的个数, 如果不大于0,表示取得所有数据
     * @return domain + "/NewClass/Folders?levelId=" + levelId
     */
    public static String getFolders(int levelId) {
        return domain + "/NewClass/Folders?levelId=" + levelId;
    }

    /**
     * 把一个网站的相对路径转为这个网站的标准HTTP全路径
     *
     * @param path
     * @return
     */
    public static String getFullPath(String path) {

        return domain + path;
    }

    /**
     * /NewClass/GetLatestPackage
     *
     * @return
     */
    public static String getLatest() {

        return domain + "/NewClass/GetLatestPackage";
    }

    public static String getLevels() {
        return domain + "/NewClass/levels";
    }

}
