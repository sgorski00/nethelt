package pl.sgorski.nethelt.webapi.http.client.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubEmailEntry(String email, @JsonProperty(value = "primary") boolean isPrimary) {}
