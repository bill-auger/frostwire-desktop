package com.limegroup.gnutella.downloader;

import java.io.File;
import java.util.Arrays;

import org.limewire.io.InvalidDataException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.downloader.serial.DownloadMemento;
import com.limegroup.gnutella.version.DownloadInformation;

@Singleton
public class CoreDownloaderFactoryImpl implements CoreDownloaderFactory {

    @Inject
    public CoreDownloaderFactoryImpl() {
    }

    public ManagedDownloader createManagedDownloader(RemoteFileDesc[] files,
            GUID originalQueryGUID, File saveDirectory, String fileName, boolean overwrite)
            throws SaveLocationException {
//        ManagedDownloader md = managedDownloaderFactory.get();
//        md.addInitialSources(Arrays.asList(files), fileName);
//        md.setQueryGuid(originalQueryGUID);
//        md.setSaveFile(saveDirectory, fileName, overwrite);
        return null;
    }

    public MagnetDownloader createMagnetDownloader(MagnetOptions magnet, boolean overwrite,
            File saveDirectory, String fileName) throws SaveLocationException {
//        if (!magnet.isDownloadable())
//            throw new IllegalArgumentException("magnet not downloadable");
//        if (fileName == null)
//            fileName = magnet.getFileNameForSaving();
//
//        MagnetDownloader md = magnetDownloaderFactory.get();
//        md.addInitialSources(null, fileName);
//        md.setSaveFile(saveDirectory, fileName, overwrite);
//        md.setMagnet(magnet);
//        return md;
        return null;
    }

    public InNetworkDownloader createInNetworkDownloader(DownloadInformation info, File dir,
            long startTime) throws SaveLocationException {
//        InNetworkDownloader id = inNetworkDownloaderFactory.get();
//        id.addInitialSources(null, info.getUpdateFileName());
//        id.setSaveFile(dir, info.getUpdateFileName(), true);
//        id.initDownloadInformation(info, startTime);
//        return id;
        return null;
    }

    public ResumeDownloader createResumeDownloader(File incompleteFile, String name, long size)
            throws SaveLocationException {
//        ResumeDownloader rd = resumeDownloaderFactory.get();
//        rd.addInitialSources(null, name);
//        rd.setSaveFile(null, name, false);
//        rd.initIncompleteFile(incompleteFile, size);
//        return rd;
        return null;
    }

    public CoreDownloader createFromMemento(DownloadMemento memento) throws InvalidDataException {
        try {
            Provider<? extends CoreDownloader> coreFactory = providerForMemento(memento);
            CoreDownloader downloader = coreFactory.get();
            downloader.initFromMemento(memento);
            return downloader;
        } catch (ClassCastException cce) {
            throw new InvalidDataException("invalid memento!", cce);
        }
    }

    private Provider<? extends CoreDownloader> providerForMemento(DownloadMemento memento)
            throws InvalidDataException {
        switch (memento.getDownloadType()) {
//        case INNETWORK:
//            return inNetworkDownloaderFactory;
//        case MAGNET:
//            return magnetDownloaderFactory;
//        case MANAGED:
//            return managedDownloaderFactory;
        case TORRENTFETCHER:
        default:
            throw new InvalidDataException("invalid memento type: " + memento.getDownloadType());
        }

    }

}
