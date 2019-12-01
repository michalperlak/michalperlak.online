interface UserRepository extends JpaRepository<User, Long>, ExtendedQuerydslPredicateExecutor<User> {
}