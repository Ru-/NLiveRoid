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

package com.flazr.rtmp.client;


import static com.flazr.rtmp.client.Amf0Value.MetaDataValuesType.MAP;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;

import android.util.Log;

import com.flazr.rtmp.message.TypeEnum;
import com.flazr.util.Utils;
import com.flazr.util.ValueToEnum;

public class Amf0Value {


    private Amf0Value() {}

    public static enum MetaDataValuesType implements TypeEnum {

        NUMBER(0x00),
        BOOLEAN(0x01),
        STRING(0x02),
        OBJECT(0x03),
        NULL(0x05),
        UNDEFINED(0x06),
        MAP(0x08),
        ARRAY(0x0A),
        DATE(0x0B),
        LONG_STRING(0x0C),
        UNSUPPORTED(0x0D);


        private final int value;

        private MetaDataValuesType(int value) {
            this.value = value;
        }
        @Override
        public int intValue() {
            return value;
        }

        private static final ValueToEnum<MetaDataValuesType> converter = new ValueToEnum<MetaDataValuesType>(MetaDataValuesType.values());

        public static MetaDataValuesType valueToEnum(final int value) {//TypeTypeの列挙がconverterに入る
            return converter.valueToEnum(value);
        }

        private static MetaDataValuesType getType(final Object value) {
            if (value == null) {
                return NULL;
            } else if (value instanceof String) {
                return STRING;
            } else if (value instanceof Number) {
                return NUMBER;
            } else if (value instanceof Boolean) {
                return BOOLEAN;
            } else if (value instanceof Amf0Object) {
                return OBJECT;
            } else if (value instanceof Map) {
                return MAP;
            } else if (value instanceof Object[]) {
                return ARRAY;
            } else if(value instanceof Date) {
                return DATE;
            } else {
                throw new RuntimeException("unexpected type: " + value.getClass());
            }
        }

    }

    private static final byte BOOLEAN_TRUE = 0x01;
    private static final byte BOOLEAN_FALSE = 0x00;
    private static final byte[] OBJECT_END_MARKER = new byte[]{0x00, 0x00, 0x09};

    public static void encode(final ChannelBuffer out, final Object value) {
        final MetaDataValuesType type = MetaDataValuesType.getType(value);
        out.writeByte((byte) type.value);
        switch (type) {
            case NUMBER:
                if(value instanceof Double) {
                    out.writeLong(Double.doubleToLongBits((Double) value));
                } else { // this coverts int also
                    out.writeLong(Double.doubleToLongBits(Double.valueOf(value.toString())));
                }
                return;
            case BOOLEAN:
                out.writeByte((Boolean) value ? BOOLEAN_TRUE : BOOLEAN_FALSE);
                return;
            case STRING:
                encodeString(out, (String) value);
                return;
            case NULL:
                return;
            case MAP:
                out.writeInt(0);
                // no break; remaining processing same as OBJECT
            case OBJECT:
                final Map<String, Object> map = (Map) value;
                for(final Map.Entry<String, Object> entry : map.entrySet()) {
                    encodeString(out, entry.getKey());
                    encode(out, entry.getValue());
                }
                out.writeBytes(OBJECT_END_MARKER,0,OBJECT_END_MARKER.length);
                return;
            case ARRAY:
                final Object[] array = (Object[]) value;
                out.writeInt(array.length);
                for(Object o : array) {
                    encode(out, o);
                }
                return;
            case DATE:
                final long time = ((Date) value).getTime();
                out.writeLong(Double.doubleToLongBits(time));
                out.writeShort((short) 0);
                return;
            default:
                // ignoring other types client doesn't require for now
                throw new RuntimeException("unexpected type: " + type);
        }
    }

    private static String decodeString(final ChannelBuffer in) {
        final short size = in.readShort();//次の2バイトにサイズが入っている
        final byte[] bytes = new byte[size];
        in.readBytes(bytes,0,size);
        return new String(bytes); // TODO UTF-8 ?
    }

    private static void encodeString(final ChannelBuffer out, final String value) {
        final byte[] bytes = value.getBytes(); // TODO UTF-8 ?
        out.writeShort((short) bytes.length);
        out.writeBytes(bytes,0,bytes.length);
    }

    public static void encode(final ChannelBuffer out, final Object... values) {
        for (final Object value : values) {
            encode(out, value);
        }
    }

    /**
     * ここでKEY=値が入る
     * BigEndianHeapChannelBufferのsuper
     * →HeapChannelBufferのsuper
     * →AbstractChannelBuffer#readByte()return getByte(readerIndex ++)
     * →HeapChannelBuffer#getByte(int index)return array[index]
     * が呼ばれている
     * @param in
     * @return
     */
    public static Object decode(final ChannelBuffer in) {
    	byte ba = in.readByte();//1バイト読み込む
        final MetaDataValuesType type = MetaDataValuesType.valueToEnum(ba);//TypeTypeの列挙の中の引数のインデックスの要素が返ってくる
        final Object value = decode(in, type);
        return value;
    }

    private static Object decode(final ChannelBuffer in, final MetaDataValuesType type) {
        switch (type) {
            case NUMBER: return Double.longBitsToDouble(in.readLong());//数値(0x00)ならLong(8バイト)Abstract#readLong→Heap#getLong読み
            case BOOLEAN: return in.readByte() == BOOLEAN_TRUE;//BOOLEAN(0x01)ならBOOLEAN_TRUE(0x01)かを返す違う場合(0x00)という事
            case STRING: return decodeString(in);//文字列(0x02)なら次の2バイトに文字列長が入っているのでその部分を文字列で返す
            case ARRAY://配列(0x0A)なら次の4バイトにサイズが入っているのでそれぞれの要素を再帰呼びする
                final int arraySize = in.readInt();
                final Object[] array = new Object[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    array[i] = decode(in);
                }
                return array;
            case MAP://MAP(0x08)とOBJECT(0x03)は同じ処置
            case OBJECT:
                final int count;
                final Map<String, Object> map;
                if(type == MAP) {
                    count = in.readInt(); //要素数が帰ってくる(メタデータは大概これに入った形になっている)
                    map = new LinkedHashMap<String, Object>();
                        Log.d("amf0Value","TYPE MAP create MAP length: "+count);
                } else {
                	Log.d("amf0Value","TYPE OBJECT create Amf0Object ");
                    count = 0;
                    map = new Amf0Object();
                }
                int i = 0;
                final byte[] endMarker = new byte[3];
                while (in.readableBytes() > 0) {//マップのKEY,VALUEのデコード
                    in.getBytes(in.readerIndex(), endMarker,0,3);
                    if (Arrays.equals(endMarker, OBJECT_END_MARKER)) {
                    	//今の位置から3バイトがエンドマーカー(0x00, 0x00, 0x09)と同一だったらブレイクする
                        in.skipBytes(3);
                            Log.d("amf0Value","Object end");
                        break;
                    }
                    if(count > 0 && i++ == count) {//要素数までカウントしてブレイクする→要素数が合っていない場合があるのでOBJECT_END_MARKERじゃないと駄目
                    	Log.d("Amf0Value","TYPE MAP Count END ------------");
//                        break;
                    }
                    map.put(decodeString(in), decode(in));//文字列と各要素(何重でも入れ子があり得る)を格納する
                }
                return map;
            case DATE://DATE(0x0B)だったら最初の8バイトが実際の日付、次の2バイトがタイムゾーン
                final long dateValue = in.readLong();
            	Log.d("Amf0Value","TYPE IS DATE"+dateValue);
                in.readShort(); // consume the timezone
                return new Date((long) Double.longBitsToDouble(dateValue));
            case LONG_STRING://LONG_STRING(0x0C)だったら最初の4バイト(普通のSTRING(0x02)とはサイズ長の型が違う)に文字列長があるのでその分を取得
            	Log.d("Amf0Value","LONG_STRING");
                final int stringSize = in.readInt();
                final byte[] bytes = new byte[stringSize];
                in.readBytes(bytes,0,stringSize);
                return new String(bytes); // TODO UTF-8 ?
            case NULL:
            	Log.d("Amf0Value","TYPE IS NULL");
            case UNDEFINED:
            	Log.d("Amf0Value","TYPE IS UNDEFINED");
            case UNSUPPORTED:
            	Log.d("Amf0Value","TYPE IS UNSUPPORTED");
                return null;
            default:
                throw new RuntimeException("unexpected type: " + type);
        }
    }

//Logでのみ利用
    private static String toString(final MetaDataValuesType type, final Object value) {
        return type+" "+value;
    }


}
