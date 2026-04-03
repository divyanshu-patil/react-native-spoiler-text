#pragma once

#include "BlurTextViewMeasurementManager.h"
#include "BlurTextViewState.h"

#include <react/renderer/components/BlurTextViewSpec/EventEmitters.h>
#include <react/renderer/components/BlurTextViewSpec/Props.h>
#include <react/renderer/components/view/ConcreteViewShadowNode.h>

namespace facebook::react {

JSI_EXPORT extern const char BlurTextViewComponentName[];
/*
 * `ShadowNode` for <BlurTextView> component.
 */
class BlurTextViewShadowNode final
    : public ConcreteViewShadowNode<BlurTextViewComponentName,
                                    BlurTextViewProps, BlurTextViewEventEmitter,
                                    BlurTextViewState> {
public:
  using ConcreteViewShadowNode::ConcreteViewShadowNode;

  // This constructor is called when we "update" shadow node, e.g. after
  // updating shadow node's state
  BlurTextViewShadowNode(ShadowNode const &sourceShadowNode,
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

  // Associates a shared `BlurTextViewMeasurementManager` with the
  // node.
  void
  setMeasurementsManager(const std::shared_ptr<BlurTextViewMeasurementManager>
                             &measurementsManager);

  void dirtyLayoutIfNeeded();

  Size
  measureContent(const LayoutContext &layoutContext,
                 const LayoutConstraints &layoutConstraints) const override;

private:
  int forceHeightRecalculationCounter_;
  std::shared_ptr<BlurTextViewMeasurementManager> measurementsManager_;
};
} // namespace facebook::react
