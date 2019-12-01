public interface QuerydslPredicateExecutor<T> {
    Optional<T> findOne(Predicate predicate);
    Iterable<T> findAll(Predicate predicate);
    Iterable<T> findAll(Predicate predicate, Sort sort);
    Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);
    Iterable<T> findAll(OrderSpecifier<?>... orders);
    Page<T> findAll(Predicate predicate, Pageable pageable);
    long count(Predicate predicate);
    boolean exists(Predicate predicate);
}