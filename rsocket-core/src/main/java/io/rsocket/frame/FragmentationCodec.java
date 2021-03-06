package io.rsocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import reactor.util.annotation.Nullable;

/** FragmentationFlyweight is used to re-assemble frames */
public class FragmentationCodec {
  public static ByteBuf encode(final ByteBufAllocator allocator, ByteBuf header, ByteBuf data) {
    return encode(allocator, header, null, data);
  }

  public static ByteBuf encode(
      final ByteBufAllocator allocator, ByteBuf header, @Nullable ByteBuf metadata, ByteBuf data) {

    final boolean hasMetadata = metadata != null;
    return FrameBodyCodec.encode(allocator, header, metadata, hasMetadata, data);
  }
}
