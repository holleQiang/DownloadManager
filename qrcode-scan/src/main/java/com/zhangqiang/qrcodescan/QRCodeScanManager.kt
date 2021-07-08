package com.zhangqiang.qrcodescan

import java.util.regex.Pattern

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-23
 */
class QRCodeScanManager {

    private val processors: MutableList<Processor> = mutableListOf()

    companion object {

        val instance: QRCodeScanManager by lazy {
            QRCodeScanManager()
        }
    }

    fun addProcessor(processor: Processor) {
        synchronized(this) {
            if (!processors.contains(processor)) {
                processors.add(processor)
            }
        }
    }

    fun removeProcessor(processor: Processor) {
        synchronized(this) {
            processors.remove(processor)
        }
    }

    fun dispatchDecodeResult(result: String): Boolean {
        var handed = false
        for (processor in processors) {
            if (processor.process(result)) {
                handed = true
            }
        }
        return handed
    }
}

interface Processor {
    fun process(text: String): Boolean
}

abstract class HttpProcessor : Processor {
    override fun process(text: String): Boolean {
        val pattern = Pattern.compile("http[s]*://.+")
        val matcher = pattern.matcher(text)
        val urls: MutableList<String> = mutableListOf()
        while (matcher.find()) {
            val url = matcher.group()
            urls.add(url)
        }
        if (urls.isNotEmpty()) {
            processHttpUrls(urls)
            return true
        }
        return false
    }

    abstract fun processHttpUrls(urls: List<String>)
}