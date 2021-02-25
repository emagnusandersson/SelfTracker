package com.gavott.backgroundlocationbroadcaster

import android.net.Uri
import android.webkit.URLUtil
import kotlin.math.abs

fun getTUnix():Long {return System.currentTimeMillis() / 1000L}

object TimeUnit{
    object TUnitLenS{const val s=1;const val m=60;const val h=3600;const val d=3600*24;const val y=3600*24*365};
    object IntMaxTimeUnit{const val s=99;const val m=99;const val h=36;const val d=365};
    object IntMaxTimeUnitS{const val s=99;const val m=99*60;const val h=36*3600;const val d=365*24*3600};
    fun getNextTick(t:Int):Int{ // t in seconds
        val tabsS=Math.abs(t);
        if(tabsS<=IntMaxTimeUnitS.s) return TUnitLenS.s;
        if(tabsS<=IntMaxTimeUnitS.s+TUnitLenS.m) return tabsS-IntMaxTimeUnitS.s;
        if(tabsS<=IntMaxTimeUnitS.m) return TUnitLenS.m;
        if(tabsS<=IntMaxTimeUnitS.m+TUnitLenS.h) return tabsS-IntMaxTimeUnitS.m;
        if(tabsS<=IntMaxTimeUnitS.h) return TUnitLenS.h;
        if(tabsS<=IntMaxTimeUnitS.h+TUnitLenS.d) return tabsS-IntMaxTimeUnitS.h;
        if(tabsS<=IntMaxTimeUnitS.d) return TUnitLenS.d;
        if(tabsS<=IntMaxTimeUnitS.d+TUnitLenS.y) return tabsS-IntMaxTimeUnitS.d;
        return TUnitLenS.y;
    }
    fun getSuitableTimeUnit(t:Int):Pair<Int, String>{ // t in seconds
        var tAbs= abs(t); val tSign=if(t>=0)1 else -1;
        if (tAbs <= IntMaxTimeUnit.s) return Pair(tSign * tAbs, "s");
        tAbs /= 60; // t in minutes
        if (tAbs <= IntMaxTimeUnit.m) return Pair(tSign * tAbs, "m");
        tAbs /= 60; // t in hours
        if (tAbs <= IntMaxTimeUnit.h) return Pair(tSign * tAbs, "h");
        tAbs /= 24; // t in days
        if (tAbs <= IntMaxTimeUnit.d) return Pair(tSign * tAbs, "d");
        tAbs /= 365; // t in years
        return Pair(tSign * tAbs, "y");
    }

//    fun getSuitableTimeUnitStr(tdiff:Int,objLang=langHtml.timeUnit,boLong=0,boArr=0){
//        var [ttmp,u]=getSuitableTimeUnit(tdiff), n=Math.round(ttmp);
//        var strU=objLang[u][boLong][Number(n!=1)];
//        if(boArr){  return [n,strU];  } else{  return n+' '+strU;   }
//    }
}

fun parseTString(str:String):Long{
    val unit=str.last(); var t=str.substring(0, -1).toLong()
    if(unit=='m') t*=60;
    else if(unit=='h') t*=3600;
    else if(unit=='d') t*=86400;
    else if(unit=='w') t*=(86400*7);
    else if(unit=='y') t*=(86400*365);
    return t
}

fun myJSEscape(str:String):String{
    return str.replace("&","&amp;").replace("<","&lt;")
}
fun processUri(strUriT:String):Triple<String?, String?, String?>{
    var strUri = strUriT.trim()
    if (!URLUtil.isValidUrl(strUri))   return Triple("url is not valid", null, null);
    if(strUri.contains(" "))   return Triple("url contains spaces", null, null);

    val uri: Uri = Uri.parse(strUri)
    if(uri.scheme!="https")   return Triple("only https allowed", null, null);
    val strUUID: String? = uri.fragment
    if(strUUID==null)   return Triple("no key", null, null);
    if(strUUID.length==0) return Triple("key length is zero", null, null);
    //if(strUUID.length!=32) return Triple("key length is not 32", null, null);
    val strUriShort=strUri.substring(0,strUri.length-strUUID.length-1)
    return Triple(null, strUriShort, strUUID)
}

fun closest2Val(arrV:Array<Int>, value:Int):Pair<Int,Int>{
    var bestFit=Int.MAX_VALUE; var best_i:Int=0;
    arrV.forEachIndexed { index, item ->
        val curFit=abs(item-value);
        if(curFit<bestFit) {bestFit=curFit; best_i=index;}
    }
    return Pair(arrV[best_i],best_i);
}