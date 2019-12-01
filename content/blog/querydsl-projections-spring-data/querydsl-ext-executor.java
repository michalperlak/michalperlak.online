public interface ExtendedQuerydslPredicateExecutor<T> extends QuerydslPredicateExecutor<T> {
    <P> Optional<P> findOne(@NonNull JPQLQuery<P> query);
    <P> Optional<P> findOne(@NonNull FactoryExpression<P> factoryExpression,
                            @NonNull Predicate predicate);
    <P> List<P> findAll(@NonNull JPQLQuery<P> query);
    <P> Page<P> findAll(@NonNull JPQLQuery<P> query, @NonNull Pageable pageable);
    <P> List<P> findAll(@NonNull FactoryExpression<P> factoryExpression,
                        @NonNull Predicate predicate);
    <P> Page<P> findAll(@NonNull FactoryExpression<P> factoryExpression,
                        @NonNull Predicate predicate,
                        @NonNull Pageable pageable);
}