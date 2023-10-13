package com.zhangqiang.downloadmanager.plugin.http.utils;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.plugin.http.range.RangePart;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HttpUtils {

    public static void setRangeParams(FiledSetter setter, long start) {
        setter.setField("Range", "bytes=" + start + "-");

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }

    public static void setRangeParams(FiledSetter setter, long start, long end) {
        setter.setField("Range", "bytes=" + start + "-" + end);

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }


    public static String parseFileName(FieldGetter fieldGetter) {

        String contentDisposition = fieldGetter.getField("Content-Disposition");
        if (!TextUtils.isEmpty(contentDisposition)) {
            String[] split = contentDisposition.split(";");
            for (String item : split) {
                int index = item.indexOf("filename=");
                if (index != -1) {
                    String fileName = item.substring(index + 9).replaceAll("\"", "");
                    try {
                        fileName = URLDecoder.decode(fileName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return fileName;
                }
            }
        }
        return null;
    }

    public static String parseETag(FieldGetter fieldGetter) {
        String value = fieldGetter.getField("ETag");
        if (!TextUtils.isEmpty(value)) {
            return value.replaceAll("\"", "");
        }
        return null;
    }

    public static String parseLastModified(FieldGetter fieldGetter) {
        String value = fieldGetter.getField("Last-Modified");
        if (!TextUtils.isEmpty(value)) {
            return value;
        }
        return null;
    }

    public static RangePart parseRangePart(FieldGetter fieldGetter) {
        String headerField = fieldGetter.getField("Content-Range");
        if (TextUtils.isEmpty(headerField)) {
            return null;
        }
        String[] split = headerField.split(" ");
        if (split.length != 2) {
            return null;
        }
        RangePart rangePart = new RangePart();
        rangePart.setUnit(split[0]);
        String[] infoSplit = split[1].split("/");
        rangePart.setTotal(Long.parseLong(infoSplit[1]));
        String rangeStr = infoSplit[0];
        String[] rangeSplit = rangeStr.split("-");
        rangePart.setStart(Long.parseLong(rangeSplit[0]));
        rangePart.setEnd(Long.parseLong(rangeSplit[1]));
        return rangePart;
    }

    public static String getFileNameSuffixByContentType(FieldGetter fieldGetter) {
        String contentTypeField = fieldGetter.getField("Content-Type");
        if (TextUtils.isEmpty(contentTypeField)) {
            return null;
        }
        String[] split = contentTypeField.split(";");
        if (split.length <= 0) {
            return null;
        }
        String contentType = split[0];
        return getFileNameSuffixByContentType(contentType);
    }

    public static String getFileNameSuffixByContentType(String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return null;
        }
        if (contentType.equals("application/vnd.android.package-archive")) {
            return "apk";
        } else if (contentType.equals("video/3gpp")) {
            return "3gp";
        } else if (contentType.equals("application/postscript")) {
            return "ai";
        } else if (contentType.equals("text/plain")) {
            return "asc";
        } else if (contentType.equals("application/atom+xml")) {
            return "atom";
        } else if (contentType.equals("audio/basic")) {
            return "au";
        } else if (contentType.equals("video/x-msvideo")) {
            return "avi";
        } else if (contentType.equals("application/x-bcpio")) {
            return "bcpio";
        } else if (contentType.equals("application/octet-stream")) {
            return "bin";
        } else if (contentType.equals("image/bmp")) {
            return "bmp";
        } else if (contentType.equals("application/x-netcdf")) {
            return "cdf";
        } else if (contentType.equals("image/cgm")) {
            return "cgm";
        } else if (contentType.equals("application/x-cpio")) {
            return "cpio";
        } else if (contentType.equals("application/mac-compactpro")) {
            return "cpt";
        } else if (contentType.equals("application/x-csh")) {
            return "csh";
        } else if (contentType.equals("text/css")) {
            return "css";
        } else if (contentType.equals("application/msword")) {
            return "doc";
        } else if (contentType.equals("application/xml-dtd")) {
            return "dtd";
        } else if (contentType.equals("application/x-dvi")) {
            return "dvi";
        } else if (contentType.equals("application/x-director")) {
            return "dxr";
        } else if (contentType.equals("text/x-setext")) {
            return "etx";
        } else if (contentType.equals("application/andrew-inset")) {
            return "ez";
        } else if (contentType.equals("video/x-flv")) {
            return "flv";
        } else if (contentType.equals("image/gif")) {
            return "gif";
        } else if (contentType.equals("application/srgs")) {
            return "gram";
        } else if (contentType.equals("application/srgs+xml")) {
            return "grxml";
        } else if (contentType.equals("application/x-gtar")) {
            return "gtar";
        } else if (contentType.equals("application/x-gzip")) {
            return "gz";
        } else if (contentType.equals("application/x-hdf")) {
            return "hdf";
        } else if (contentType.equals("application/mac-binhex40")) {
            return "hqx";
        } else if (contentType.equals("text/html")) {
            return "html";
        } else if (contentType.equals("x-conference/x-cooltalk")) {
            return "ice";
        } else if (contentType.equals("image/x-icon")) {
            return "ico";
        } else if (contentType.equals("text/calendar")) {
            return "ics";
        } else if (contentType.equals("image/ief")) {
            return "ief";
        } else if (contentType.equals("model/iges")) {
            return "iges";
        } else if (contentType.equals("application/x-java-jnlp-file")) {
            return "jnlp";
        } else if (contentType.equals("image/jp2")) {
            return "jp2";
        } else if (contentType.equals("image/jpeg")) {
            return "jpeg";
        } else if (contentType.equals("image/jpg")) {
            return "jpg";
        } else if (contentType.equals("application/x-javascript")) {
            return "js";
        } else if (contentType.equals("audio/midi")) {
            return "kar";
        } else if (contentType.equals("application/x-latex")) {
            return "latex";
        } else if (contentType.equals("audio/x-mpegurl")) {
            return "m3u";
        } else if (contentType.equals("audio/mp4a-latm")) {
            return "m4a";
        } else if (contentType.equals("video/vnd.mpegurl")) {
            return "m4u";
        } else if (contentType.equals("video/x-m4v")) {
            return "m4v";
        } else if (contentType.equals("image/x-macpaint")) {
            return "mac";
        } else if (contentType.equals("application/x-troff-man")) {
            return "man";
        } else if (contentType.equals("application/mathml+xml")) {
            return "mathml";
        } else if (contentType.equals("application/x-troff-me")) {
            return "me";
        } else if (contentType.equals("model/mesh")) {
            return "mesh";
        } else if (contentType.equals("application/vnd.mif")) {
            return "mif";
        } else if (contentType.equals("video/quicktime")) {
            return "mov";
        } else if (contentType.equals("video/x-sgi-movie")) {
            return "movie";
        } else if (contentType.equals("audio/mpeg")) {
            return "mp3";
        } else if (contentType.equals("video/mp4")) {
            return "mp4";
        } else if (contentType.equals("video/mpeg")) {
            return "mpe";
        } else if (contentType.equals("application/x-troff-ms")) {
            return "ms";
        } else if (contentType.equals("application/oda")) {
            return "oda";
        } else if (contentType.equals("application/ogg")) {
            return "ogg";
        } else if (contentType.equals("video/ogv")) {
            return "ogv";
        } else if (contentType.equals("image/x-portable-bitmap")) {
            return "pbm";
        } else if (contentType.equals("image/pict")) {
            return "pct";
        } else if (contentType.equals("chemical/x-pdb")) {
            return "pdb";
        } else if (contentType.equals("application/pdf")) {
            return "pdf";
        } else if (contentType.equals("image/x-portable-graymap")) {
            return "pgm";
        } else if (contentType.equals("application/x-chess-pgn")) {
            return "pgn";
        } else if (contentType.equals("image/png")) {
            return "png";
        } else if (contentType.equals("image/x-portable-anymap")) {
            return "pnm";
        } else if (contentType.equals("image/x-portable-pixmap")) {
            return "ppm";
        } else if (contentType.equals("application/vnd.ms-powerpoint")) {
            return "ppt";
        } else if (contentType.equals("image/x-quicktime")) {
            return "qti";
        } else if (contentType.equals("audio/x-pn-realaudio")) {
            return "ra";
        } else if (contentType.equals("image/x-cmu-raster")) {
            return "ras";
        } else if (contentType.equals("application/rdf+xml")) {
            return "rdf";
        } else if (contentType.equals("image/x-rgb")) {
            return "rgb";
        } else if (contentType.equals("application/vnd.rn-realmedia")) {
            return "rm";
        } else if (contentType.equals("application/x-troff")) {
            return "roff";
        } else if (contentType.equals("text/rtf")) {
            return "rtf";
        } else if (contentType.equals("text/richtext")) {
            return "rtx";
        } else if (contentType.equals("text/sgml")) {
            return "sgm";
        } else if (contentType.equals("application/x-sh")) {
            return "sh";
        } else if (contentType.equals("application/x-shar")) {
            return "shar";
        } else if (contentType.equals("application/x-stuffit")) {
            return "sit";
        } else if (contentType.equals("application/x-koan")) {
            return "skd";
        } else if (contentType.equals("application/smil")) {
            return "smi";
        } else if (contentType.equals("application/x-futuresplash")) {
            return "spl";
        } else if (contentType.equals("application/x-wais-source")) {
            return "src";
        } else if (contentType.equals("application/x-sv4cpio")) {
            return "sv4cpio";
        } else if (contentType.equals("application/x-sv4crc")) {
            return "sv4crc";
        } else if (contentType.equals("image/svg+xml")) {
            return "svg";
        } else if (contentType.equals("application/x-shockwave-flash")) {
            return "swf";
        } else if (contentType.equals("application/x-tar")) {
            return "tar";
        } else if (contentType.equals("application/x-tcl")) {
            return "tcl";
        } else if (contentType.equals("application/x-tex")) {
            return "tex";
        } else if (contentType.equals("application/x-texinfo")) {
            return "texi";
        } else if (contentType.equals("image/tiff")) {
            return "tif";
        } else if (contentType.equals("text/tab-separated-values")) {
            return "tsv";
        } else if (contentType.equals("application/x-ustar")) {
            return "ustar";
        } else if (contentType.equals("application/x-cdlink")) {
            return "vcd";
        } else if (contentType.equals("model/vrml")) {
            return "vrml";
        } else if (contentType.equals("application/voicexml+xml")) {
            return "vxml";
        } else if (contentType.equals("audio/x-wav")) {
            return "wav";
        } else if (contentType.equals("image/vnd.wap.wbmp")) {
            return "wbmp";
        } else if (contentType.equals("application/vnd.wap.wbxml")) {
            return "wbxml";
        } else if (contentType.equals("video/webm")) {
            return "webm";
        } else if (contentType.equals("text/vnd.wap.wml")) {
            return "wml";
        } else if (contentType.equals("application/vnd.wap.wmlc")) {
            return "wmlc";
        } else if (contentType.equals("text/vnd.wap.wmlscript")) {
            return "wmls";
        } else if (contentType.equals("application/vnd.wap.wmlscriptc")) {
            return "wmlsc";
        } else if (contentType.equals("video/x-ms-wmv")) {
            return "wmv";
        } else if (contentType.equals("image/x-xbitmap")) {
            return "xbm";
        } else if (contentType.equals("application/xhtml+xml")) {
            return "xht";
        } else if (contentType.equals("application/vnd.ms-excel")) {
            return "xls";
        } else if (contentType.equals("application/xml")) {
            return "xml";
        } else if (contentType.equals("image/x-xpixmap")) {
            return "xpm";
        } else if (contentType.equals("application/xslt+xml")) {
            return "xslt";
        } else if (contentType.equals("application/vnd.mozilla.xul+xml")) {
            return "xul";
        } else if (contentType.equals("image/x-xwindowdump")) {
            return "xwd";
        } else if (contentType.equals("chemical/x-xyz")) {
            return "xyz";
        } else if (contentType.equals("application/zip")) {
            return "zip";
        }
        return null;
    }

}
