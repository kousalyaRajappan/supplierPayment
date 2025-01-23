package za.co.topitup.suppliers.network

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class pos_users  (


    var posuser_id:Int = 0,
    var posuser_firstname: String? = null,
    var posuser_surname: String? = null,
    var posuser_isadmin: Int? = null,
    var posuser_status: Int? = null,
    var posuser_pin: String? = null,
    var posuser_pin_swipe: String? = null,
    var posuser_create_dtm: String? = null,
    var posuser_edit_dtm: String? = null,
    var rica_training: String? = null,
    var rica_registered: String? = null,
    var posuser_idnumber_type: String? = null,
    var posuser_idnumber: String? = null

)
