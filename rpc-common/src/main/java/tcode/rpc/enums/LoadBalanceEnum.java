package tcode.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {

    RANDOM("random"),
    consistent("consistent");
    private final String name;
}
