package com.zhangqiang.downloadmananger.template

import java.io.*

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-16
 */
object ContentTypeMappings {

     fun generateFileNameSuffixContentTypeMapping() {

        var br: BufferedReader? = null
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(
                File(
                    "./template/src/main/java/com/zhangqiang/downloadmananger/template/HttpUtils.java"
                ), false
            )
            fos.write("public static String getFileNameSuffixByContentType(String contentType){\n".toByteArray())
            fos.write("\tif(TextUtils.isEmpty(contentType)){\n".toByteArray())
            fos.write("\t\treturn null;\n".toByteArray())
            fos.write("\t}\n".toByteArray())
            val inputStream: InputStream = FileInputStream(File("./template/fileNameSuffix"))
            br = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var ifCalled = false
            while (br.readLine().also { line = it } != null) {
                val mapSlit = line!!.split("\\s+".toRegex()).toTypedArray()
                if (ifCalled) {
                    fos.write("else ".toByteArray())
                }
                fos.write(
                    String.format("if(contentType.equals(\"%s\")){\n", mapSlit[1]).toByteArray()
                )
                fos.write(String.format("\t\treturn \"%s\";\n", mapSlit[0]).toByteArray())
                fos.write(String.format("\t\t}").toByteArray())
                ifCalled = true
            }
            fos.write("return null;\n".toByteArray())
            fos.write("}\n".toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            br?.close()
            fos?.close()
        }
    }


}

fun main() {
    ContentTypeMappings.generateFileNameSuffixContentTypeMapping()
}