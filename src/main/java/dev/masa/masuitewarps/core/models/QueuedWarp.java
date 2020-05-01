package dev.masa.masuitewarps.core.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class QueuedWarp {

    private final Warp warp;
    private final boolean silent;
}
