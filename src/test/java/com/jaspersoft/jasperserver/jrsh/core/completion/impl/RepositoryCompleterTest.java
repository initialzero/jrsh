package com.jaspersoft.jasperserver.jrsh.core.completion.impl;

import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jaspersoft.jasperserver.jrsh.core.common.SessionFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

/**
 * Created by serhii.blazhyievskyi on 8/13/2015.
 */
public class RepositoryCompleterTest {

    @Mock
    private Session sessionMock;

    @Spy
    private RepositoryCompleter completerSpy = new RepositoryCompleter();

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        SessionFactory.updateSharedSession(sessionMock);
    }

    @Test
    public void shouldReturnEmptyStringIfLastInputIsSlash() throws Exception {
        //When
        String lastInput = completerSpy.getLastInput("someTestString/");

        //Then
        assertEquals("", lastInput);
    }

    @Test
    public void shouldReturnStringTestIfLastInputIsNotSlash() throws Exception {
        //When
        String lastInput = completerSpy.getLastInput("someTestString/Test");

        //Then
        assertEquals("Test", lastInput);
    }

    @Test
    public void shouldBeTrueForEveryCandidateStartsWithMask() throws Exception {
        //Given
        String mask = "test";
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        CharSequence candidate1 = "testAndFast";
        CharSequence candidate2 = "testButLast";
        CharSequence candidate3 = "testPest";
        candidates.add(candidate1);
        candidates.add(candidate2);
        candidates.add(candidate3);

        //When
        Method method = RepositoryCompleter.class.getDeclaredMethod("compareCandidatesWithLastInput", String.class, List.class);
        method.setAccessible(true);

        //Then
        assertTrue((Boolean) method.invoke(completerSpy, mask, candidates));
    }

    @Test
    public void shouldBeFalseIfAnyCandidateNotStartsWithMask() throws Exception {
        //Given
        String mask = "test";
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        CharSequence candidate1 = "testAndFast";
        CharSequence candidate2 = "definitelyNotOneYouLookFor";
        CharSequence candidate3 = "testPest";
        candidates.add(candidate1);
        candidates.add(candidate2);
        candidates.add(candidate3);

        //When
        Method method = RepositoryCompleter.class.getDeclaredMethod("compareCandidatesWithLastInput", String.class, List.class);
        method.setAccessible(true);

        //Then
        assertFalse((Boolean) method.invoke(completerSpy, mask, candidates));
    }

    @Test
    public void shouldReturnBufferLength() throws Exception {
        //Given
        String buffer = "testSomeLongerString";
        int cursorPos = 5; //somebody try to complete input while cursor isn\t at the end of string
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        //When
        int result = completerSpy.complete(buffer, cursorPos, candidates);

        //Then
        assertEquals(result, buffer.length());
    }

    @Test
    public void shouldReturnZeroForNonZeroUniqueIdEqualsToHashAndEmptyBuffer() throws Exception {
        //Given
        String buffer = null;
        int cursorPos = 0;
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        //When
        int result = completerSpy.complete(buffer, cursorPos, candidates);

        //Then
        assertEquals(result, 0);
    }

    @Test
    public void shouldReturnBufferLengthPlusOne() throws Exception {
        //Given
        String buffer = "testSomeLongerString";
        int cursorPos = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        Pair<String, Boolean> pair = new ImmutablePair<String, Boolean>(buffer, true);
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair);

        doReturn(pairList).when(completerSpy).download(buffer);
        doReturn(true).when(completerSpy).isResourceExist(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursorPos, candidates);

        //Then
        assertEquals(result, buffer.length() + 1);
    }

    @Test
    public void shouldReturnPositionNextAfterSlash() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        String root = "testSomeOtherStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        List<Pair<String, Boolean>> emptyList = new ArrayList<Pair<String, Boolean>>();

        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
//        PowerMockito.mockStatic(RepositoryDownloader.class);
//        PowerMockito.when(RepositoryDownloader.download(buffer)).thenReturn(emptyList);
//        PowerMockito.when(RepositoryDownloader.download(root)).thenReturn(emptyList);
//        PowerMockito.when(RepositoryDownloader.isResourceExist(buffer)).thenReturn(false);
//        PowerMockito.when(RepositoryDownloader.isResourceExist(root)).thenReturn(false);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnBufferLengthPlusOneWithEmptyResultFromDownloader() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();

        doReturn(pairList).when(completerSpy).download(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnBufferLengthPlusOneWithEmptyResultFromDownloaderButFoundLastPath() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        String root = "testSomeOtherStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        Pair<String, Boolean> pair = new ImmutablePair<String, Boolean>(buffer, true);
        List<Pair<String, Boolean>> emptyList = new ArrayList<Pair<String, Boolean>>();
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair);

        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(pairList).when(completerSpy).download(root);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
        doReturn(true).when(completerSpy).isResourceExist(root);
//        PowerMockito.mockStatic(RepositoryDownloader.class);
//        PowerMockito.when(RepositoryDownloader.download(buffer)).thenReturn(emptyList);
//        PowerMockito.when(RepositoryDownloader.download(root)).thenReturn(pairList);
//        PowerMockito.when(RepositoryDownloader.isResourceExist(buffer)).thenReturn(false);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnLastInputLengthForSlashBufferAndNotEmptyDownloader() throws Exception {
        //Given
        String buffer = "/";
        String lastInput = completerSpy.getLastInput(buffer);
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add(lastInput + "0");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        Pair<String, Boolean> pair1 = new ImmutablePair<String, Boolean>(lastInput + "1", true);
        Pair<String, Boolean> pair2 = new ImmutablePair<String, Boolean>(lastInput + "2", true);
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair1);
        pairList.add(pair2);

        doReturn(pairList).when(completerSpy).download(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.length() - lastInput.length());
    }

    @Test
    public void shouldReturnBufferLengthForSlashBufferAndEmptyDownloader() throws Exception {
        //Given
        String buffer = "/";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, completerSpy.hashCode());

        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();

        doReturn(pairList).when(completerSpy).download(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.length());
    }

    @Test
    public void shouldReturnZeroForUniqueIdDifferentFromHashAndEmptyBuffer() throws Exception {
        //Given
        String buffer = null;
        int cursor = 0;
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 1);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, 0);
    }

    @Test
    public void shouldReturnLastInputLengthForUniqueIdDifferentFromHashAndNotEmptyBuffer() throws Exception {
        //Given
        String buffer = "/";
        String lastInput = completerSpy.getLastInput(buffer);
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add(lastInput + "0");
        candidates.add(lastInput + "1");

        Field uniqueId = RepositoryCompleter.class.getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 1);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.length() - lastInput.length());
    }

    @Test
    public void shouldReturnZeroForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = null;
        int cursorPos = 0;
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        //When
        int result = completerSpy.complete(buffer, cursorPos, candidates);

        //Then
        assertEquals(result, 0);
    }

    @Test
    public void shouldReturnBufferLengthPlusOneForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = "testSomeLongerString";
        int cursorPos = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();

        Field uniqueId = RepositoryCompleter.class.getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        Pair<String, Boolean> pair = new ImmutablePair<String, Boolean>(buffer, true);
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair);

        doReturn(pairList).when(completerSpy).download(buffer);
        doReturn(true).when(completerSpy).isResourceExist(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursorPos, candidates);

        //Then
        assertEquals(result, buffer.length() + 1);
    }

    @Test
    public void shouldReturnPositionNextAfterSlashForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        String root = "testSomeOtherStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        List<Pair<String, Boolean>> emptyList = new ArrayList<Pair<String, Boolean>>();

        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
//        PowerMockito.mockStatic(RepositoryDownloader.class);
//        PowerMockito.when(RepositoryDownloader.download(buffer)).thenThrow(new ResourceNotFoundException());
//        PowerMockito.when(RepositoryDownloader.download(root)).thenThrow(new ResourceNotFoundException());

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
//        PowerMockito.verifyStatic();
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnBufferLengthPlusOneWithEmptyResultFromDownloaderForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();

        doReturn(pairList).when(completerSpy).download(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnBufferLengthPlusOneWithEmptyResultFromDownloaderButFoundLastPathForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = "testSomeLongerStringW/Slash";
        String root = "testSomeOtherStringW/Slash";
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add("test");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        Pair<String, Boolean> pair = new ImmutablePair<String, Boolean>(buffer, true);
        List<Pair<String, Boolean>> emptyList = new ArrayList<Pair<String, Boolean>>();
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair);

        doReturn(emptyList).when(completerSpy).download(buffer);
        doReturn(pairList).when(completerSpy).download(root);
        doReturn(false).when(completerSpy).isResourceExist(buffer);
        doReturn(true).when(completerSpy).isResourceExist(root);
//        PowerMockito.mockStatic(RepositoryDownloader.class);
//        PowerMockito.when(RepositoryDownloader.download(buffer)).thenThrow(new ResourceNotFoundException());
//        PowerMockito.when(RepositoryDownloader.download(root)).thenReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.lastIndexOf("/") + 1);
    }

    @Test
    public void shouldReturnLastInputLengthForSlashBufferAndNotEmptyDownloaderForUniqueIdEqualZero() throws Exception {
        //Given
        String buffer = "/";
        String lastInput = completerSpy.getLastInput(buffer);
        int cursor = buffer.length();
        List<CharSequence> candidates = new ArrayList<CharSequence>();
        candidates.add(lastInput + "0");

        Field uniqueId = RepositoryCompleter.class.
                getDeclaredField("uniqueId");
        uniqueId.setAccessible(true);
        uniqueId.set(completerSpy, 0);

        Pair<String, Boolean> pair1 = new ImmutablePair<String, Boolean>(lastInput + "1", true);
        Pair<String, Boolean> pair2 = new ImmutablePair<String, Boolean>(lastInput + "2", true);
        List<Pair<String, Boolean>> pairList = new ArrayList<Pair<String, Boolean>>();
        pairList.add(pair1);
        pairList.add(pair2);

        doReturn(pairList).when(completerSpy).download(buffer);
//        PowerMockito.stub(PowerMockito.method(RepositoryDownloader.class, "download")).toReturn(pairList);

        //When
        int result = completerSpy.complete(buffer, cursor, candidates);

        //Then
        assertEquals(result, buffer.length() - lastInput.length());
    }

    @After
    public void after() throws Exception {
        Mockito.reset(
                sessionMock
        );
    }
}