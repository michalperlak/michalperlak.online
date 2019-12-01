public class QuerydslJpaRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable>
        extends JpaRepositoryFactoryBean<R, T, ID> {

    public QuerydslJpaRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    @NonNull
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new QuerydslJpaRepositoryFactory(entityManager);
    }

    private static class QuerydslJpaRepositoryFactory extends JpaRepositoryFactory {
        QuerydslJpaRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        @NonNull
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, @NonNull EntityManager entityManager) {
            if (isQueryDslRepository(information)) {
                return new QuerydslJpaBaseRepository<>(getEntityInformation(information.getDomainType()), entityManager);
            }
            return super.getTargetRepository(information, entityManager);
        }

        @Override
        @NonNull
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            if (isQueryDslRepository(metadata)) {
                return QuerydslJpaBaseRepository.class;
            }
            return super.getRepositoryBaseClass(metadata);
        }

        private boolean isQueryDslRepository(RepositoryMetadata metadata) {
            Class<?> repositoryInterface = metadata.getRepositoryInterface();
            return ExtendedQuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
        }
    }
}