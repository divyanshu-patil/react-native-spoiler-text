#pragma once

#include "SpoilerTextViewMeasurementManager.h"
#include "SpoilerTextViewState.h"

#include <react/renderer/components/SpoilerTextViewSpec/EventEmitters.h>
#include <react/renderer/components/SpoilerTextViewSpec/Props.h>
#include <react/renderer/components/view/ConcreteViewShadowNode.h>

namespace facebook::react {

JSI_EXPORT extern const char SpoilerTextViewComponentName[];
/*
 * `ShadowNode` for <SpoilerTextView> component.
 */
class SpoilerTextViewShadowNode final
    : public ConcreteViewShadowNode<SpoilerTextViewComponentName,
                                    SpoilerTextViewProps, SpoilerTextViewEventEmitter,
                                    SpoilerTextViewState> {
public:
  using ConcreteViewShadowNode::ConcreteViewShadowNode;

  // This constructor is called when we "update" shadow node, e.g. after
  // updating shadow node's state
  SpoilerTextViewShadowNode(ShadowNode const &sourceShadowNode,
                         ShadowNodeFragment const &fragment)
      : ConcreteViewShadowNode(sourceShadowNode, fragment) {
    dirtyLayoutIfNeeded();
  }

  static ShadowNodeTraits BaseTraits() {
    auto traits = ConcreteViewShadowNode::BaseTraits();
    traits.set(ShadowNodeTraits::Trait::LeafYogaNode);
    traits.set(ShadowNodeTraits::Trait::MeasurableYogaNode);
    return traits;
  }

  // Associates a shared `SpoilerTextViewMeasurementManager` with the
  // node.
  void
  setMeasurementsManager(const std::shared_ptr<SpoilerTextViewMeasurementManager>
                             &measurementsManager);

  void dirtyLayoutIfNeeded();

  Size
  measureContent(const LayoutContext &layoutContext,
                 const LayoutConstraints &layoutConstraints) const override;

private:
  int forceHeightRecalculationCounter_;
  std::shared_ptr<SpoilerTextViewMeasurementManager> measurementsManager_;
};
} // namespace facebook::react
