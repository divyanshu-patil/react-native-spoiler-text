#pragma once

#include <folly/dynamic.h>
#include <react/renderer/components/SpoilerTextViewSpec/Props.h>

namespace facebook::react {

inline folly::dynamic toDynamic(const SpoilerTextViewProps &props) {
  folly::dynamic d = folly::dynamic::object();
  d["fontSize"] = props.fontSize;
  d["fontWeight"] = props.fontWeight;
  d["fontStyle"] = props.fontStyle;
  d["fontFamily"] = props.fontFamily;
  return d;
}

} // namespace facebook::react
