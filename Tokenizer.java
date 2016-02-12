package org.oikw.json;

import java.io.Reader;
import java.io.IOException;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

public class Tokenizer {
    Reader reader;
    int buffered = -1;
    char[] hexbuf = new char[4];

    StringBuilder tokenBuf = new StringBuilder();
    String tokenStr;
    double tokenNum;

    public Tokenizer(Reader reader) {
        this.reader = reader;
    }

    public String getStringToken() { return this.tokenStr; }
    public double getNumberToken() { return this.tokenNum; }

    public TokenType next() throws IOException, IllegalArgumentException {
        int c = this.buffered;
        this.buffered = -1;
        while (true) {
            if (c < 0) {
                c = this.reader.read();
                if (c < 0)
                    return TokenType.EOS;
            }

            // skip whitespace
            if (c == 0x20 || c == 0x09 || c == 0x0a || c == 0x0d) {
                c = -1;
                continue;
            }

            // return structural characters
            if (c == 0x5b)
                return TokenType.BeginArray;
            if (c == 0x7b)
                return TokenType.BeginObject;
            if (c == 0x5d)
                return TokenType.EndArray;
            if (c == 0x7d)
                return TokenType.EndObject;
            if (c == 0x3a)
                return TokenType.NameSeparator;
            if (c == 0x2c)
                return TokenType.ValueSeparator;

            // return string
            if (c == 0x22) {
                while (true) {
                    c = this.reader.read();
                    if (c < 0)
                        throw new IllegalArgumentException();
                    if (c == 0x22) {
                        this.tokenStr = this.tokenBuf.toString();
                        this.tokenBuf.delete(0, this.tokenBuf.length());
                        return TokenType.String;
                    }
                    if (c == 0x5c) {
                        c = this.reader.read();
                        if (c == 0x22 || c == 0x5c || c == 0x2f) {
                            // pass through
                        } else if (c == 0x62) {
                            c = 0x08;
                        } else if (c == 0x66) {
                            c = 0x0c;
                        } else if (c == 0x6e) {
                            c = 0x0a;
                        } else if (c == 0x72) {
                            c = 0x0d;
                        } else if (c == 0x74) {
                            c = 0x09;
                        } else if (c == 0x75) {
                            for (int i = 0; i < 4;) {
                                int ret = this.reader.read(this.hexbuf, i, 4 - i);
                                if (ret < 0)
                                    throw new IllegalArgumentException();
                                i += ret;
                            }
                            c = Integer.parseInt(new String(this.hexbuf, 0, 4), 16);
                        } else {
                            throw new IllegalArgumentException();
                        }
                        this.tokenBuf.append((char)c);
                        continue;
                    }
                    this.tokenBuf.append((char)c);
                }
            }

            // return number
            if (c == 0x2d /* minus */ || (c >= 0x30 && c <= 0x39 /* zero/digit1-9 */)) {
                if (c != 0x30) {
                    // digit1-9 *DIGIT
                    do {
                        this.tokenBuf.append((char)c);
                        c = this.reader.read();
                    } while (c >= 0x30 && c <= 0x39);
                } else {
                    // zero
                    this.tokenBuf.append((char)c);
                    c = this.reader.read();
                }
                if (c == 0x2e) {
                    // frac
                    this.tokenBuf.append((char)c);
                    c = this.reader.read();
                    if (c < 0x30 || c > 0x39)
                        throw new IllegalArgumentException();
                    do {
                        this.tokenBuf.append((char)c);
                        c = this.reader.read();
                    } while (c >= 0x30 && c <= 0x39);
                }
                if (c == 0x65 || c == 0x45) {
                    // exp
                    this.tokenBuf.append((char)c);
                    c = this.reader.read();
                    if (c == 0x2d || c == 0x2b) {
                        this.tokenBuf.append((char)c);
                        c = this.reader.read();
                    }
                    if (c < 0x30 || c > 0x39)
                        throw new IllegalArgumentException();
                    do {
                        this.tokenBuf.append((char)c);
                        c = this.reader.read();
                    } while (c >= 0x30 && c <= 0x39);
                }
                this.buffered = c;
                this.tokenNum = Double.parseDouble(this.tokenBuf.toString());
                this.tokenBuf.delete(0, this.tokenBuf.length());
                return TokenType.Number;
            }

            // true
            if (c == 0x74) {
                if (this.reader.read() != 0x72 ||
                    this.reader.read() != 0x75 ||
                    this.reader.read() != 0x65)
                    throw new IllegalArgumentException();
                return TokenType.True;
            }

            // false
            if (c == 0x66) {
                if (this.reader.read()!= 0x61 ||
                    this.reader.read() != 0x6c ||
                    this.reader.read() != 0x73 ||
                    this.reader.read() != 0x65)
                    throw new IllegalArgumentException();
                return TokenType.False;
            }

            // null
            if (c == 0x6e) {
                if (this.reader.read() != 0x75 ||
                    this.reader.read() != 0x6c ||
                    this.reader.read() != 0x6c)
                    throw new IllegalArgumentException();
                return TokenType.Null;
            }

            throw new IllegalArgumentException();
        }
    }

    public enum TokenType {
        BeginArray,
        BeginObject,
        EndArray,
        EndObject,
        NameSeparator,
        ValueSeparator,
        True,
        False,
        Null,
        Number,
        String,
        EOS
    }
}
