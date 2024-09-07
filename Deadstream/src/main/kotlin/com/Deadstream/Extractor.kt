package com.Deadstream

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.Filesim
import com.lagradost.cloudstream3.extractors.Voe

class MyFileMoon : Filesim() {
    override var mainUrl = "https://filemoon.nl"
}

class MyVoe : Voe() {
    override var mainUrl = "https://jessicaglassauthor.com"
}
