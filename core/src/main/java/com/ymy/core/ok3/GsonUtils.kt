package com.ymy.core.ok3

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Created on 2020/9/10 10:23.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object GsonUtils {
    val mGson: Gson =
        GsonBuilder().registerTypeAdapterFactory(NullStringEmptyTypeAdapterFactory()).create()

    fun string2JsonObject(str: String): JsonObject {
        return JsonParser.parseString(str).asJsonObject
    }
}

class NullStringEmptyTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson?, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType as Class<T>
        return if (rawType != String::class.java) {
            null
        } else StringNullAdapter() as TypeAdapter<T>
    }
}


class StringNullAdapter : TypeAdapter<String>() {

    override fun write(writer: JsonWriter, value: String?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.value(value)
    }

    override fun read(reader: JsonReader): String {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return ""
        }
        return reader.nextString();
    }

}