/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PhantomMenaceTestCase {

  private static final int COUNT = 10 * 1000;

  private List<WeakReference<String>> objects;
  private List<PhantomReference<String>> phantoms;


  @Test
  public void phantomMenace() throws Exception {
    objects = new ArrayList<>(COUNT);
    phantoms = new ArrayList<>(COUNT);
    ReferenceQueue<String> referenceQueue = new ReferenceQueue<>();

    assertThat(referenceQueue.remove(5000), is(nullValue()));

    for (int i = 0; i < COUNT; i++) {
      String value = randomAlphanumeric(1024);
      objects.add(new WeakReference<>(value));
      phantoms.add(new PhantomReference<>(value, referenceQueue));
    }

    System.gc();

    int collectedCount = 0;

    while (collectedCount < COUNT) {
      System.out.println("Polling");
      Reference<?> referenceFromQueue = referenceQueue.remove(5000);
      if (referenceFromQueue != null) {
        assertThat(objects.get(collectedCount).get(), is(nullValue()));
        System.out.println("Collected: " + ++collectedCount);

        referenceFromQueue.clear();
      }
    }
  }

  @Test
  public void hollywoodPrinciple() throws Exception {
    objects = new ArrayList<>(COUNT);
    phantoms = new ArrayList<>(COUNT);
    ReferenceQueue<String> referenceQueue = new ReferenceQueue<>();

    new Thread(() -> {
      try {
        Thread.sleep(4000);
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
      for (int i = 0; i < COUNT; i++) {
        String value = randomAlphanumeric(1024);
        objects.add(new WeakReference<>(value));
        phantoms.add(new PhantomReference<>(value, referenceQueue));
      }

      System.gc();
    }).start();

    int collectedCount = 0;
    Reference<?> referenceFromQueue = referenceQueue.remove(15000);
    if (referenceFromQueue != null) {
      assertThat(objects.get(collectedCount).get(), is(nullValue()));
      System.out.println("Collected: " + ++collectedCount);

      referenceFromQueue.clear();
    } else {
      fail();
    }

    while (collectedCount < COUNT)  {
      System.out.println("Polling");
      referenceFromQueue = referenceQueue.remove(5000);
      if (referenceFromQueue != null) {
        assertThat(objects.get(collectedCount).get(), is(nullValue()));
        System.out.println("Collected: " + ++collectedCount);

        referenceFromQueue.clear();
      }
    }
  }
}
