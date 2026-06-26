package worldcupbraket.domain.livematch;

public record LiveMatchResult(
  Sport sport,
  String state,
  String displayTitle,
  ContestInfo contestInfo,
  DateTimeInfo dateTimeInfo,
  Competitor competitor1,
  Competitor competitor2
){}

