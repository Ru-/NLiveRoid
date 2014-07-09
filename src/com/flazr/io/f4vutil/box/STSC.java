/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.io.f4vutil.box;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;

import android.util.Log;

import com.flazr.io.f4vutil.Payload;

public class STSC implements Payload {

    public static class STSCRecord {

        private int firstChunk;
        private int samplesPerChunk;
        private int sampleDescIndex;

        public int getFirstChunk() {
            return firstChunk;
        }

        public int getSamplesPerChunk() {
            return samplesPerChunk;
        }

        public int getSampleDescIndex() {
            return sampleDescIndex;
        }

    }

    private List<STSCRecord> records;

    public STSC(ChannelBuffer in) {
        read(in);
    }

    public List<STSCRecord> getRecords() {
        return records;
    }

    public void setRecords(List<STSCRecord> records) {
        this.records = records;
    }

    @Override
    public void read(ChannelBuffer in) {
        in.readInt(); // UI8 version + UI24 flags
        final int count = in.readInt();
        Log.d("no of sample chunk records: {}", ""+count);
        records = new ArrayList<STSCRecord>(count);
        for (int i = 0; i < count; i++) {
            final STSCRecord record = new STSCRecord();
            record.firstChunk = in.readInt();
            record.samplesPerChunk = in.readInt();
            record.sampleDescIndex = in.readInt();
//            Log.d("#{} firstChunk: {} samplesPerChunk: {} sampleDescIndex: {}",
//                    new Object[]{i, record.firstChunk, record.samplesPerChunk, record.sampleDescIndex});
            records.add(record);
        }
    }

    @Override
    public ChannelBuffer write() {
        ChannelBuffer out = new DynamicChannelBuffer(ChannelBuffers.BIG_ENDIAN,256);
        out.writeInt(0); // UI8 version + UI24 flags
        out.writeInt(records.size());
        for (STSCRecord record : records) {
            out.writeInt(record.firstChunk);
            out.writeInt(record.samplesPerChunk);
            out.writeInt(record.sampleDescIndex);
        }
        return out;
    }

}
