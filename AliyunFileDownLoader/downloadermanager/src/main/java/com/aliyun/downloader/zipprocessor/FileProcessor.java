/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.downloader.zipprocessor;

import java.io.File;

public interface FileProcessor {
	
	File process(File file);

}
