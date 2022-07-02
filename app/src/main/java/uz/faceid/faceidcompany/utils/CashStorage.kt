package evote.uz.evoteuz.utils

import android.content.Context
import android.content.SharedPreferences

object CashStorage {
    fun addToken(context: Context, token: String) {
        /** yozish uchun */
        val sharedrefrence = context.getSharedPreferences("Token", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedrefrence.edit()
        editor.putString("token", token).commit()
    }

    fun getToken(context: Context): String {
        /** uqish uchun */
        val sharedrefrence = context.getSharedPreferences("Token", Context.MODE_PRIVATE)
        return sharedrefrence.getString("token", "").toString()
    }

    fun addID(context: Context, id: String) {
        val sharedPreferences = context.getSharedPreferences("ID", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("id", id).commit()
    }

    fun getID(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("ID", Context.MODE_PRIVATE)
        return sharedPreferences.getString("id", "0").toString()
    }

    /**change lang*/
    fun addLanguage(context: Context, lang: String) {
        val sharedPreferences = context.getSharedPreferences("Lang", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("lang", lang).commit()
    }

    /**get lang*/
    fun getLang(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Lang", Context.MODE_PRIVATE)
        return sharedPreferences.getString("lang", "uz").toString()
    }
}