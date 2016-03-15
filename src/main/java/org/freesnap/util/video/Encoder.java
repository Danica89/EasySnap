/*
 * FreeSnap - multiplatform desktop application, allows to make, edit and share screenshots.
 *
 * Copyright (C) 2016 Kamil Karkus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freesnap.util.video;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Encoder {
    private SeekableByteChannel ch;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo = 0;
    private MP4Muxer muxer;

    public Encoder(File out, int width, int height) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);
        _out = ByteBuffer.allocate(width * height * 6);
        encoder = new H264Encoder();
        spsList = new ArrayList<ByteBuffer>();
        ppsList = new ArrayList<ByteBuffer>();


        this.ch = NIOUtils.writableFileChannel(out);
        muxer = new MP4Muxer(ch, Brand.MP4);
        outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 25);
    }

    public void encodeImage(BufferedImage bi) throws IOException {
        // Encode image into H.264 frame, the result is stored in '_out' buffer
        _out.clear();
        ByteBuffer result = encoder.encodeFrame(_out, makeFrame(bi));

        // Based on the frame above form correct MP4 packet
        spsList.clear();
        ppsList.clear();
        H264Utils.encodeMOVPacket(result, spsList, ppsList);

        // Add packet to video track
        outTrack.addFrame(new MP4Packet(result, frameNo, 25, 1, frameNo, true, null, frameNo, 0));

        frameNo++;
    }

    // make a YUV420J out of BufferedImage pixels
    private Picture makeFrame(BufferedImage bi) {
        DataBuffer imageData = bi.getRaster().getDataBuffer();
        int[] yPixel = new int[imageData.getSize()];
        int[] uPixel = new int[imageData.getSize() >> 2];
        int[] vPixel = new int[imageData.getSize() >> 2];
        int ipx = 0, uvOffset = 0;

        for (int h = 0; h < bi.getHeight(); h++) {
            for (int w = 0; w < bi.getWidth(); w++) {
                int elem = imageData.getElem(ipx);
                int r = 0x0ff & (elem >>> 16);
                int g = 0x0ff & (elem >>> 8);
                int b = 0x0ff & elem;
                yPixel[ipx] = ((66 * r + 129 * g + 25 * b) >> 8) + 16;
                if ((0 != w % 2) && (0 != h % 2)) {
                    uPixel[uvOffset] = ((-38 * r + -74 * g + 112 * b) >> 8) + 128;
                    vPixel[uvOffset] = ((112 * r + -94 * g + -18 * b) >> 8) + 128;
                    uvOffset++;
                }
                ipx++;
            }
        }
        int[][] pix = {yPixel, uPixel, vPixel, null};
        return new Picture(bi.getWidth(), bi.getHeight(), pix, ColorSpace.YUV420);
    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4
        outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));

        // Write MP4 header and finalize recording
        muxer.writeHeader();
        NIOUtils.closeQuietly(ch);
    }
}