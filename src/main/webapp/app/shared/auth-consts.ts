export class AuthConsts {

    /*
    View Pages
    */

    public static readonly VIEW_USERS_PAGE = "VIEW_USERS_PAGE";
    public static readonly VIEW_CREATE_USERS_PAGE = "VIEW_CREATE_USERS_PAGE";
    public static readonly VIEW_EDIT_USERS_PAGE = "VIEW_EDIT_USERS_PAGE";


    public static readonly VIEW_MY_PROFILE_PAGE = "VIEW_MY_PROFILE_PAGE";

    public static readonly VIEW_DASHBOARD_PAGE = "VIEW_DASHBOARD_PAGE";

    public static readonly VIEW_DP_PAGE = "VIEW_DP_PAGE";
    public static readonly VIEW_CREATE_DP_PAGE = "VIEW_CREATE_DP_PAGE";
    public static readonly VIEW_EDIT_DP_PAGE = "VIEW_EDIT_DP_PAGE";

    public static readonly VIEW_SUB_PAGE = "VIEW_SUB_PAGE";
    public static readonly VIEW_CREATE_SUB_PAGE = "VIEW_CREATE_SUB_PAGE";
    public static readonly VIEW_EDIT_SUB_PAGE = "VIEW_EDIT_SUB_PAGE";

    public static readonly VIEW_DRIVER_PAGE = "VIEW_DRIVER_PAGE";
    public static readonly VIEW_PAYMENT_PAGE = "VIEW_PAYMENT_PAGE";

    public static readonly VIEW_REPORT_PAGE = "VIEW_REPORT_PAGE";
    public static readonly VIEW_TRIP_REPORT_PAGE = "VIEW_TRIP_REPORT_PAGE";
    public static readonly CREATE_KPI_REPORT_BY_DP = "CREATE_KPI_REPORT_BY_DP";

    public static readonly VIEW_TRIPS_PAGE = "VIEW_TRIPS_PAGE";
    public static readonly VIEW_DISPATCHES_PAGE = "VIEW_DISPATCHES_PAGE";
    public static readonly VIEW_DELAYED_DISPATCHES_PAGE = "VIEW_DELAYED_DISPATCHES_PAGE";
    public static readonly CREATE_DELAYED_DISPATCHES = "CREATE_DELAYED_DISPATCHES";
    public static readonly VIEW_INCIDENT_PAGE = "VIEW_INCIDENT_PAGE";
    public static readonly VIEW_DELIVERY_REQUESTS_PAGE = "VIEW_DELIVERY_REQUESTS_PAGE";

    public static readonly VIEW_CITY_PAGE = "VIEW_CITY_PAGE";
    public static readonly VIEW_CREATE_CITY_PAGE = "VIEW_CREATE_CITY_PAGE";
    public static readonly VIEW_EDIT_CITY_PAGE = "VIEW_EDIT_CITY_PAGE";

    public static readonly VIEW_MAP_FULLSCREEN_PAGE = "VIEW_MAP_FULLSCREEN_PAGE";

    public static readonly VIEW_CITY_BOUNDARIES_PAGE = "VIEW_CITY_BOUNDARIES_PAGE";
    public static readonly VIEW_GENERAL_CONFIG_PAGE = "VIEW_GENERAL_CONFIG_PAGE";

    public static readonly VIEW_HEAT_MAP_PAGE = "VIEW_HEAT_MAP_PAGE";
    public static readonly VIEW_DP_MAP_FULLSCREEN_PAGE = "VIEW_DP_MAP_FULLSCREEN_PAGE";
    public static readonly VIEW_DP_DASHBOARD_PAGE = "VIEW_DP_DASHBOARD_PAGE";
    /*
        All Endpoints of Delivery Provider.
         */
    public static readonly GET_ALL_DP = "GET_ALL_DP";
    public static readonly ADD_DP = "ADD_DP";
    public static readonly GENERATE_UNIQUE_CODE = "GENERATE_UNIQUE_CODE";
    public static readonly IS_DEFAULT_EXIST = "IS_DEFAULT_EXIST";
    public static readonly DELETE_DP = "DELETE_DP";
    public static readonly GET_DP_BY_ID = "GET_DP_BY_ID";
    public static readonly UPDATE_DP = "UPDATE_DP";
    public static readonly UPDATE_ACTIVE_STATUS = "UPDATE_ACTIVE_STATUS";
    public static readonly CREATE_DP_REP_USER_BY_DP = "CREATE_DP_REP_USER_BY_DP";
    public static readonly GET_ALL_DP_EXCERPT = "GET_ALL_DP_EXCERPT";

    /*
    All Endpoints of Dispatch.
     */
    public static readonly GET_ALL_DISPATCH = "GET_ALL_DISPATCH";
    public static readonly CREATE_DISPATCH = "CREATE_DISPATCH";
    public static readonly GET_DELIVERY_PROMISE = "GET_DELIVERY_PROMISE";
    public static readonly GET_DISPATCH_BY_SUBSCRIBER = "GET_DISPATCH_BY_SUBSCRIBER";
    public static readonly UPDATE_DISPATCH = "UPDATE_DISPATCH";
    public static readonly VIEW_PICKUP_LOCATION_PAGE = "VIEW_PICKUP_LOCATION_PAGE";
    public static readonly ADD_PICKUP_LOCATION = "ADD_PICKUP_LOCATION";
    public static readonly UPDATE_PICKUP_LOCATION = "UPDATE_PICKUP_LOCATION";

    /*
    All Endpoints of Driver.
     */
    public static readonly GET_ALL_DRIVERS = "GET_ALL_DRIVERS";
    public static readonly GET_ALL_DRIVERS_DP = "GET_ALL_DRIVERS_DP";
    public static readonly ME = "ME";
    public static readonly GET_BY_MOBILE_AND_OTHER = "GET_BY_MOBILE_AND_OTHER";
    public static readonly GET_NEAREST_DRIVER = "GET_NEAREST_DRIVER";
    public static readonly UPDATE_NOTIFICATION_TOKEN = "UPDATE_NOTIFICATION_TOKEN";
    public static readonly CHANGE_STATUS = "CHANGE_STATUS";
    public static readonly GET_DRIVER_REG = "GET_DRIVER_REG";
    public static readonly REG_DRIVER = "REG_DRIVER";
    public static readonly EDIT_DRIVER_REG = "EDIT_DRIVER_REG";
    public static readonly UPDATE_REG_STATUS = "UPDATE_REG_STATUS";
    public static readonly GET_DRIVERS_BY_REG_STATUS = "GET_DRIVERS_BY_REG_STATUS";
    public static readonly GET_DRIVER = "GET_DRIVER";
    public static readonly CHANGE_ACTIVE_STATUS = "CHANGE_ACTIVE_STATUS";
    public static readonly CHANGE_CONNECTED_STATUS = "CHANGE_CONNECTED_STATUS";
    public static readonly SEND_NOTIFICATION = "SEND_NOTIFICATION";
    public static readonly REVIEW_DRIVER = "REVIEW_DRIVER";
    public static readonly GET_ALL_REVIEWS = "GET_ALL_REVIEWS";
    public static readonly UPDATE_DRIVER_LIMIT = "UPDATE_DRIVER_LIMIT";
    public static readonly UPDATE_DRIVER_ATTENDANCE_FLAG = "UPDATE_DRIVER_ATTENDANCE_FLAG";
    public static readonly UPDATE_DRIVER_TRIP_REWARD_AMOUNT = "UPDATE_DRIVER_TRIP_REWARD_AMOUNT";
    public static readonly UPDATE_DELIVERY_SUPPORT_AMOUNT = "UPDATE_DELIVERY_SUPPORT_AMOUNT";

    /*
    All Endpoints of PAYMENT.
    */
    public static readonly CREATE_PAYMENT = "CREATE_PAYMENT";
    public static readonly SETTLE_DRIVER_PAYMENT = "SETTLE_DRIVER_PAYMENT";


    /*
    All Endpoints of Delivery Request.
     */
    public static readonly GET_DELIVERY_REQ_BY_DISPATCH = "GET_DELIVERY_REQ_BY_DISPATCH";
    public static readonly UPDATE_DELIVERY_REQ = "UPDATE_DELIVERY_REQ";

    /*
    All Endpoints of Authorization.
     */
    public static readonly GET_PRINCIPLE = "GET_PRINCIPLE";
    public static readonly EXTRACT_TOKEN = "EXTRACT_TOKEN";
    public static readonly LOGOUT = "LOGOUT";

    /*
    All Endpoints of City.
     */
    public static readonly GET_ALL_CITIES = "GET_ALL_CITIES";
    public static readonly CREATE_CITY = "CREATE_CITY";
    public static readonly DELETE_CITY = "DELETE_CITY";
    public static readonly GET_CITY_BY_ID = "GET_CITY_BY_ID";
    public static readonly UPDATE_CITY = "UPDATE_CITY";

    /*
    All Endpoints of Dashboard.
     */
    public static readonly GET_INIT_DATA = "GET_INIT_DATA";

    /*
    All Endpoints of Dashboard WebSocket Session.
    */
    public static readonly GET_ALL_WSS = "GET_ALL_WSS";
    public static readonly ADD_WSS = "ADD_WSS";
    public static readonly UPDATE_WSS = "UPDATE_WSS";
    public static readonly UPDATE_WSS_BY_ID = "UPDATE_WSS_BY_ID";
    public static readonly DELETE_WSS = "DELETE_WSS";
    public static readonly GET_WSS = "GET_WSS";


    /*
    All Endpoints of Driver Weight.
    */
    public static readonly GET_ALL_WEIGHT = "GET_ALL_WEIGHT";
    public static readonly CREATE_DRIVER_WEIGHT = "CREATE_DRIVER_WEIGHT";
    public static readonly UPDATE_DRIVER_WEIGHT = "UPDATE_DRIVER_WEIGHT";

    /*
    All Endpoints of Elastic Search.
    */
    public static readonly GET_CITY_IF_POINT_WITHIN = "GET_CITY_IF_POINT_WITHIN";
    public static readonly SAVE_CITY_BOUNDARIES = "SAVE_CITY_BOUNDARIES";
    public static readonly GET_CITY_BOUNDARIES = "GET_CITY_BOUNDARIES";

    /*
    All Endpoints of Fare Method.
    */
    public static readonly GET_ALL_FARE_METHODS = "GET_ALL_FARE_METHODS";
    public static readonly ADD_FARE_METHOD = "ADD_FARE_METHOD";
    public static readonly DELETE_FARE_METHOD = "DELETE_FARE_METHOD";
    public static readonly GET_FARE_METHOD = "GET_FARE_METHOD";
    public static readonly UPDATE_FARE_METHOD = "UPDATE_FARE_METHOD";

    /*
    All Endpoints of Map Element.
    */
    public static readonly GET_BY_KEY_WORD = "GET_BY_KEY_WORD";
    public static readonly GET_DRIVER_INFO = "GET_DRIVER_INFO";
    public static readonly GET_WITHIN_CIRCLE = "GET_WITHIN_CIRCLE";
    public static readonly GET_ELEMENT_BY_TYPE = "GET_ELEMENT_BY_TYPE";
    public static readonly GET_DRIVER_INFO_BY_USER_ID = "GET_DRIVER_INFO_BY_USER_ID";

    /*
    All Endpoints of Map WebSocket Session.
    */
    public static readonly GET_ALL_MAP_WSS = "GET_ALL_MAP_WSS";
    public static readonly ADD_MAP_WSS = "ADD_MAP_WSS";
    public static readonly UPDATE_MAP_WSS = "UPDATE_MAP_WSS";
    public static readonly UPDATE_MAP_WSS_BY_ID = "UPDATE_MAP_WSS_BY_ID";
    public static readonly DELETE_MAP_WSS = "DELETE_MAP_WSS";
    public static readonly GET_MAP_WSS = "GET_MAP_WSS";


    /*
    All Endpoints of S3.
    */
    public static readonly DOWNLOAD = "DOWNLOAD";
    public static readonly LIST = "LIST";

    /*
    All Endpoints of Subscriber.
    */
    public static readonly GET_ALL_SUB = "GET_ALL_SUB";
    public static readonly ADD_SUB = "ADD_SUB";
    public static readonly DELETE_SUB = "DELETE_SUB";
    public static readonly GET_SUB = "GET_SUB";
    public static readonly UPDATE_SUB = "UPDATE_SUB";
    public static readonly UPDATE_ACTIVE_STATUS_SUB = "UPDATE_ACTIVE_STATUS_SUB";
    public static readonly CREATE_SUB_REP_USER_BY_SUB = "CREATE_SUB_REP_USER_BY_SUB";
    public static readonly GET_ALL_SUB_EXCERPT = "GET_ALL_SUB_EXCERPT";

    /*
    All Endpoints of System Config.
    */
    public static readonly GET_ALL_SYSTEM_CONF = "GET_ALL_SYSTEM_CONF";
    public static readonly CREATE_SYSTEM_CONF = "CREATE_SYSTEM_CONF";
    public static readonly UPDATE_SYSTEM_CONF = "UPDATE_SYSTEM_CONF";

    /*
    All Endpoints of Trips.
    */
    public static readonly GET_ALL_TRIPS = "GET_ALL_TRIPS";
    public static readonly GET_ALL_INCIDENTS = "GET_ALL_INCIDENTS";
    public static readonly CREATE_INCIDENTS = "CREATE_INCIDENTS";
    public static readonly GET_TRIP_BY_ID = "GET_TRIP_BY_ID";
    public static readonly UPDATE_TRIP = "UPDATE_TRIP";
    public static readonly GET_POSSIBLE_INCIDENT_TYPES = "GET_POSSIBLE_INCIDENT_TYPES";
    public static readonly GET_ALL_TRIP_STATUS = "GET_ALL_TRIP_STATUS";
    public static readonly GET_TRIP_AUDIT = "GET_TRIP_AUDIT";

    /*
    All Endpoints of User.
    */
    public static readonly SEARCH_USERS = "SEARCH_USERS";
    public static readonly UPDATE_USER = "UPDATE_USER";
    public static readonly RESET_USER_AVATER = "RESET_USER_AVATER";
    public static readonly UPDATE_USER_PROFILE = "UPDATE_USER_PROFILE";
    public static readonly GET_USER_BY_USERNAME = "GET_USER_BY_USERNAME";
    public static readonly GET_USER_BY_ID = "GET_USER_BY_ID";
    public static readonly CREATE_SWIFT_USER = "CREATE_SWIFT_USER";
    public static readonly CREATE_DP_REP_USER = "CREATE_DP_REP_USER";
    public static readonly CREATE_SUB_REP_USER = "CREATE_SUB_REP_USER";
    public static readonly GET_ALL_ROLES_IDENTITY = "GET_ALL_ROLES_IDENTITY";


    /*
    All Endpoints of Vehicle.
    */
    public static readonly CREATE_VEHICLE = "CREATE_VEHICLE";
    public static readonly GET_VEHICLE_TYPES = "GET_VEHICLE_TYPES";
    public static readonly DELETE_VEHICLE = "DELETE_VEHICLE";
    public static readonly GET_VEHICLE = "GET_VEHICLE";
    public static readonly UPDATE_VEHICLE = "UPDATE_VEHICLE";

    /*
    All Endpoints of Vehicle Info Lookup.
    */
    public static readonly GET_ALL_MAKES = "GET_ALL_MAKES";
    public static readonly GET_ALL_COLORS = "GET_ALL_COLORS";
    public static readonly GET_ALL_MODELS = "GET_ALL_MODELS";

    /**
     *  Endpoint for swift properties/ configuration controller
     * */

    public static readonly GET_NEAREST_CONFIG = "GET_NEAREST_CONFIG";
    public static readonly UPDATE_NEAREST_CONFIG = "UPDATE_NEAREST_CONFIG";

    /*
    All Endpoints of Representative Controller.
    */

    public static readonly GET_REP_BY_USER_ID = "GET_REP_BY_USER_ID";

}