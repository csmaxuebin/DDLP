function [pr_post] = exp_inference(p_prior, z, DeltaX, eps, T,MAX,z_true)

     Delta=T*DeltaX';% each column is a possible answer;
     [d,N]=size(T);
     if(size(DeltaX,1)~=1 )
         pr=sum(DeltaX);
     else
         pr=DeltaX;
     end

if(MAX==0)
   pr_post=p_prior;
   return;
end

     col=1;
     sum_pr=0;
     for i=1:N
         if pr(1,i)==1

             pr(1,i)=(eps/(2*MAX)) *exp(eps*norm(z-Delta(:,col))/(2*MAX)); %change
            % pr(1,i)=exp(eps*norm(z-Delta(:,col))/(2*MAX));
             col=col+1;
             sum_pr=sum_pr+pr(1,i);
         end
     end
    for i=1:N
       pr(1,i)=pr(1,i)/sum_pr;
    end

     pr_post=pr.*p_prior;
     pr_post=pr_post/sum(pr_post);   %Bayesian inference;

     if(sum(pr_post)==0 || sum(pr_post==NaN)>0)
         pr_post=p_prior;
     end
end